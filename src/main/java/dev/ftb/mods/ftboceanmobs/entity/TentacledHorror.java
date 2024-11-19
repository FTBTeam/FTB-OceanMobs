package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.mobai.ChaseTargetGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import dev.ftb.mods.ftboceanmobs.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

public class TentacledHorror extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation GRAB_START_ANIMATION = RawAnimation.begin().thenPlay("attack.grab_start");
    private static final RawAnimation GRAB_HOLD_ANIMATION = RawAnimation.begin().thenPlay("attack.grab_hold");
    private static final RawAnimation GRAB_BREAK_ANIMATION = RawAnimation.begin().thenPlay("attack.grab_break");
    private static final RawAnimation INKING_ANIMATION = RawAnimation.begin().thenPlay("attack.ink");

    protected static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(TentacledHorror.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Boolean> DATA_INK_SPRAY = SynchedEntityData.defineId(TentacledHorror.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET = SynchedEntityData.defineId(TentacledHorror.class, EntityDataSerializers.INT);
    private long nextInkTime = 0L;
    private LivingEntity clientSideCachedAttackTarget;

    public TentacledHorror(EntityType<? extends TentacledHorror> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.27F)
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, 4F)
                .add(Attributes.ARMOR_TOUGHNESS, 2F)
                .add(Attributes.FOLLOW_RANGE, 48F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75F)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    public static boolean isPlayerPassenger(Player player) {
        return player.getVehicle() instanceof TentacledHorror && !player.isCreative();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level) {
            @Override
            protected PathFinder createPathFinder(int maxVisitedNodes) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, maxVisitedNodes) {
                    @Override
                    protected float distance(Node first, Node second) {
                        return first.distanceToXZ(second);
                    }
                };
            }
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TentacleGrabGoal(this));
        this.goalSelector.addGoal(1, new InkAttackGoal(this));
        this.goalSelector.addGoal(2, new ChaseTargetGoal(this, 1.4, 48f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_STATE, AttackState.NONE.id());
        builder.define(DATA_INK_SPRAY, false);
        builder.define(DATA_ATTACK_TARGET, 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 10, this::walkIdleState));
        controllers.add(new AnimationController<>(this, "Attacking", 5, this::attackState));
    }

    public boolean hasSyncedTarget() {
        return entityData.get(DATA_ATTACK_TARGET) != 0;
    }

    public void setSyncedTarget(@Nullable LivingEntity target) {
        entityData.set(DATA_ATTACK_TARGET, target == null ? 0 : target.getId());
    }

    @Nullable
    public LivingEntity getSyncedTarget() {
        if (!this.hasSyncedTarget()) {
            return null;
        } else if (this.level().isClientSide) {
            if (this.clientSideCachedAttackTarget != null) {
                return this.clientSideCachedAttackTarget;
            } else {
                Entity entity = this.level().getEntity(this.entityData.get(DATA_ATTACK_TARGET));
                if (entity instanceof LivingEntity) {
                    this.clientSideCachedAttackTarget = (LivingEntity)entity;
                    return this.clientSideCachedAttackTarget;
                } else {
                    return null;
                }
            }
        } else {
            return this.getTarget();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (level().isClientSide) {
            if (DATA_INK_SPRAY.equals(key) && entityData.get(DATA_INK_SPRAY) && getSyncedTarget() != null) {
                MiscUtil.doParticleSpray(this, getSyncedTarget(), ModParticleTypes.HORROR_INK.get(), 50);
            } else if (DATA_ATTACK_TARGET.equals(key)) {
                clientSideCachedAttackTarget = null;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (super.hurt(source, amount)) {
            if (getAttackState().isGrabbing() && (source.is(DamageTypeTags.IS_FIRE) || random.nextInt(8) == 0)) {
                setAttackState(AttackState.GRAB_BREAK);
            }
        }
        return false;
    }

    private PlayState walkIdleState(AnimationState<TentacledHorror> state) {
        if (state.isMoving()) {
            state.setAnimation(DefaultAnimations.WALK);
            state.setControllerSpeed(1.5f);
        } else {
            state.setAnimation(DefaultAnimations.IDLE);
            state.setControllerSpeed(1f);
        }
        return PlayState.CONTINUE;
    }

    private PlayState attackState(AnimationState<TentacledHorror> state) {
        return getAttackState().playState(state);
    }

    public AttackState getAttackState() {
        return AttackState.BY_ID.apply(entityData.get(DATA_STATE));
    }

    public void setAttackState(AttackState state) {
        entityData.set(DATA_STATE, state.id());
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        float rot = Mth.wrapDegrees(yBodyRot) + 70;
        double xOff = Mth.cos(rot * (float) (Math.PI / 180.0)) * 7.0;
        double zOff = Mth.sin(rot * (float) (Math.PI / 180.0)) * 7.0;

        return super.getPassengerAttachmentPoint(entity, dimensions, partialTick)
                .add(xOff, -4, zOff);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.TENTACLE_SQUISH.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return distanceToClosestPlayer > 128.0 * 128.0;
    }

    static class TentacleGrabGoal extends Goal {
        private static final int WARMUP_TIME = 40;
        private static final int COOLDOWN_TIME = 30;

        private final TentacledHorror horror;
        private LivingEntity target;
        private int warmupCounter;
        private int cooldownCounter;

        TentacleGrabGoal(TentacledHorror horror) {
            this.horror = horror;

            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            target = horror.getTarget();
            if (target == null) {
                return false;
            }
            return target.isAlive() && (targetInGrabRange() || target.getVehicle() == horror);
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive()
                    && (targetInGrabRange() || target.getVehicle() == horror)
                    && cooldownCounter > 0;
        }

        @Override
        public void start() {
            cooldownCounter = reducedTickDelay(COOLDOWN_TIME);
            if (target.getVehicle() == horror) {
                // this can happen when reloading world and horror is already holding someone
                warmupCounter = 0;
                horror.setAttackState(AttackState.GRAB_HOLD);
            } else {
                warmupCounter = reducedTickDelay(WARMUP_TIME);
                horror.setAttackState(AttackState.GRAB_START);
            }
        }

        @Override
        public void stop() {
            horror.setAttackState(AttackState.NONE);
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }

        @Override
        public void tick() {
            if (target.isOnFire()) {
                horror.setAttackState(AttackState.GRAB_BREAK);
            }

            switch (horror.getAttackState()) {
                case GRAB_START -> {
                    horror.getLookControl().setLookAt(target);
                    if (warmupCounter > 0 && --warmupCounter == 0) {
                        if (target != null && target.isAlive() && ableToGrab()) {
                            // do the grab!
                            horror.setAttackState(AttackState.GRAB_HOLD);
                            target.startRiding(horror, true);
                        } else {
                            // player got out of the way during warmup?
                            horror.setAttackState(AttackState.GRAB_BREAK);
                        }
                    }
                }
                case GRAB_HOLD -> {
                    if (target.getRandom().nextInt(20) == 0) {
                        // some crushing damage to the player periodically
                        target.hurt(target.level().damageSources().mobAttack(horror), 8f);
                    }
                    if (horror.random.nextInt(100) == 0) {
                        // sometimes just let go
                        horror.setAttackState(AttackState.GRAB_BREAK);
                    } else {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WARMUP_TIME, 3, false, false));
                    }
                }
                case GRAB_BREAK -> {
                    if (target.getVehicle() == horror) {
                        // let go and yeet the player in a random direction
                        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                        target.stopRiding();
                        target.setOnGround(false);
                        RandomSource r = horror.random;
                        target.setDeltaMovement(new Vec3(r.nextFloat() - 0.5f, r.nextFloat() * 0.2f + 0.15f, r.nextFloat() - 0.5f).scale(10.5));
                        if (target instanceof ServerPlayer sp) {
                            sp.connection.send(new ClientboundSetEntityMotionPacket(target));
                        }
                        target.level().playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE);
                    }
                    if (cooldownCounter > 0 && --cooldownCounter == 0) {
                        cooldownCounter--;
                    }
                }
            }
        }

        private boolean targetInGrabRange() {
            // if player is close enough to start (not necessarily complete) a grab
            return horror.distanceToSqr(target) < 49.0;
        }

        private boolean ableToGrab() {
            // if player is close enough to the grabbing tentacle, and doesn't manage to evade
            double chance = 1.0;

            Vec3 grabPos = horror.getPassengerRidingPosition(target).add(0, -5, 0);
            double dist = target.position().distanceTo(grabPos);
            if (dist > 3.0) {
                // reduce chance if more than 3 blocks from grab centre
                double x = (dist - 3.0) / 8.0;
                chance -= x;
            }
            // reduce chance more if crouching and/or blocking
            if (target.isCrouching()) {
                chance -= 0.1;
            }
            if (target.isBlocking()) {
                chance -= 0.1;
            }

            return horror.getRandom().nextDouble() < Math.max(0.2, chance);
        }
    }

    static class InkAttackGoal extends Goal {
        private static final int INK_ATTACK_TIME = 40;

        private final TentacledHorror horror;
        private LivingEntity target;
        private Vec3 inkTarget;
        private int tickCounter;

        public InkAttackGoal(TentacledHorror horror) {
            this.horror = horror;

            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            target = horror.getTarget();
            if (target == null) {
                return false;
            }
            return target.isAlive()
                    && horror.distanceToSqr(target) > 64.0
                    && target.getRandom().nextFloat() < 0.05f
                    && horror.tickCount > horror.nextInkTime;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive() && tickCounter >= 0;
        }

        @Override
        public void start() {
            inkTarget = target.position();

            tickCounter = reducedTickDelay(INK_ATTACK_TIME);

            horror.getNavigation().stop();
            horror.setAttackState(AttackState.INKING);
            horror.setSyncedTarget(target);
        }

        @Override
        public void stop() {
            horror.setAttackState(AttackState.NONE);
            horror.setSyncedTarget(null);
            horror.entityData.set(DATA_INK_SPRAY, false);
            horror.nextInkTime = horror.tickCount + 40L;
        }

        @Override
        public void tick() {
            horror.getLookControl().setLookAt(target);

            tickCounter--;

            if (tickCounter == 10) {
                // trigger client to play spray particles
                horror.entityData.set(DATA_INK_SPRAY, true);
            } else if (tickCounter == 6) {
                inkTarget = inkTarget.lerp(target.position(), 0.5);
                AreaEffectCloud cloud = new AreaEffectCloud(target.level(), inkTarget.x, inkTarget.y, inkTarget.z);
                cloud.setPotionContents(new PotionContents(Optional.empty(), Optional.of(0xFF000000), List.of()));
                cloud.setOwner(horror);
                cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                cloud.setRadius(4.0f);
                cloud.setDuration(150);
                cloud.setRadiusOnUse(-0.5f);
                cloud.setWaitTime(20);
                cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
                horror.level().playSound(null, BlockPos.containing(inkTarget), SoundEvents.SLIME_JUMP, SoundSource.HOSTILE);
                horror.level().addFreshEntity(cloud);
            }
        }
    }

    @EventBusSubscriber(modid = FTBOceanMobs.MODID)
    public static class Listener {
        @SubscribeEvent
        public static void onHurt(LivingIncomingDamageEvent event) {
            if (event.getEntity().getVehicle() instanceof TentacledHorror) {
                // grabbed players take extra damage
                event.setAmount(event.getAmount() * 1.25f);
            }
        }
    }

    public enum AttackState {
        NONE(null),
        GRAB_START(GRAB_START_ANIMATION, 1.25f),
        GRAB_HOLD(GRAB_HOLD_ANIMATION),
        GRAB_BREAK(GRAB_BREAK_ANIMATION),
        INKING(INKING_ANIMATION, 2f);

        private final RawAnimation animation;
        private final float controllerSpeed;

        static final IntFunction<AttackState> BY_ID = ByIdMap.continuous(AttackState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        AttackState(@Nullable RawAnimation animation) {
            this(animation, 1f);
        }

        AttackState(@Nullable RawAnimation animation, float controllerSpeed) {
            this.animation = animation;
            this.controllerSpeed = controllerSpeed;
        }

        byte id() {
            return (byte) ordinal();
        }

        PlayState playState(AnimationState<TentacledHorror> aState) {
            if (aState == null || this == NONE) {
                return PlayState.STOP;
            }
            aState.setControllerSpeed(controllerSpeed);
            return aState.setAndContinue(animation);
        }

        boolean isGrabbing() {
            return this == GRAB_HOLD || this == GRAB_START;
        }
    }
}
