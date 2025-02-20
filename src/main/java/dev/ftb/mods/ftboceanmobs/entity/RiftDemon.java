package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.mobai.DelayedMeleeAttackGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.function.Function;

public class RiftDemon extends BaseRiftMob {
    private static final RawAnimation ANIM_ATTACK_GLARE = RawAnimation.begin().thenPlay("attack.glare");

    protected static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(RiftDemon.class, EntityDataSerializers.BYTE);
    private static final byte STATE_NONE = 0;
    private static final byte STATE_SHIELD_UP = 1;
    private static final byte STATE_GLARING = 2;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RiftDemon(EntityType<? extends RiftDemon> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.ARMOR, 12F)
                .add(Attributes.ARMOR_TOUGHNESS, 8F)
                .add(Attributes.FOLLOW_RANGE, 48F)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new ShieldsUpGoal(this));
        goalSelector.addGoal(1, new GlareGoal(this));
        goalSelector.addGoal(2, new DelayedMeleeAttackGoal(this, 1.3, false, 18));

        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(new AnimationController<>(this, "Attacking", 10, this::attackState));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_STATE, STATE_NONE);
    }

    @Override
    public boolean isBlocking() {
        return isShieldUp();
    }

    @Override
    protected void blockUsingShield(LivingEntity attacker) {
        attacker.hurt(level().damageSources().hotFloor(), 8f);
        attacker.igniteForTicks(30);
        playSound(SoundEvents.SHIELD_BLOCK, 1f, 1f);
    }

    private PlayState attackState(AnimationState<RiftDemon> state) {
        state.setControllerSpeed(1f);
        if (isShieldUp()) {
            return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
        } else if (isGlaring()) {
            return state.setAndContinue(ANIM_ATTACK_GLARE);
        } else if (swinging) {
            return state.setAndContinue(DefaultAnimations.ATTACK_STRIKE);
        }
        return PlayState.STOP;
    }

    public boolean isShieldUp() {
        return entityData.get(DATA_STATE) == STATE_SHIELD_UP;
    }

    public boolean isGlaring() {
        return entityData.get(DATA_STATE) == STATE_GLARING;
    }

    public void setShieldUp() {
        entityData.set(DATA_STATE, STATE_SHIELD_UP);
    }

    public void setGlaring() {
        entityData.set(DATA_STATE, STATE_GLARING);
    }

    private void clearState() {
        entityData.set(DATA_STATE, STATE_NONE);
    }

    @Override
    public int getCurrentSwingDuration() {
        return 30;
    };

    @Override
    protected AABB getAttackBoundingBox() {
        // long arms...
        return super.getAttackBoundingBox().inflate(2.0);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            entity.igniteForSeconds(4);
            return true;
        }
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.PIGLIN_BRUTE_STEP, 1f, 0.75f);
    }

    @Override
    public void playDelayedAttackSound() {
        playSound(ModSounds.RIFT_DEMON_ATTACK.get());
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFT_DEMON_DEATH.get();
    }

    @Override
    protected float nextStep() {
        return moveDist + 1.2f;
    }

    private boolean couldBlock() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.distanceToSqr(position()) >= 25.0) {
            return false;
        }
        Vec3 targetPos = target.position();
        Vec3 viewVec = calculateViewVector(0.0F, getYHeadRot());
        Vec3 offset = targetPos.vectorTo(position());
        offset = new Vec3(offset.x, 0.0, offset.z).normalize();
        return offset.dot(viewVec) < 0.0;
    }

    private boolean checkGlareDist() {
        if (getTarget() == null) return false;
        double d2 = distanceToSqr(getTarget());
        return d2 >= 36.0 && d2 <= 256.0;
    }

    private static abstract class AnimatedActionGoal extends Goal {
        protected final RiftDemon riftDemon;
        private final int ticks;
        private final int minInterval;
        private final Function<RiftDemon,Boolean> useCheck;
        private final Runnable onStart;

        protected int tickCounter;
        protected LivingEntity target;
        private int nextUseTick;

        public AnimatedActionGoal(RiftDemon riftDemon, int ticks, int minInterval, Function<RiftDemon,Boolean> useCheck, Runnable onStart) {
            this.riftDemon = riftDemon;
            this.ticks = ticks;
            this.minInterval = minInterval;
            this.useCheck = useCheck;
            this.onStart = onStart;

            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            target = riftDemon.getTarget();
            return riftDemon.tickCount >= nextUseTick && useCheck.apply(riftDemon);
        }

        @Override
        public boolean canContinueToUse() {
            return riftDemon.getTarget() != null && target.isAlive() && tickCounter > 0;
        }

        @Override
        public void start() {
            onStart.run();
            tickCounter = reducedTickDelay(ticks);
        }

        @Override
        public void stop() {
            riftDemon.clearState();
            nextUseTick = riftDemon.tickCount + minInterval;
        }

        @Override
        public void tick() {
            riftDemon.getLookControl().setLookAt(target);
            if (tickCounter > 0) {
                tickCounter--;
            }
        }
    }

    public static class ShieldsUpGoal extends AnimatedActionGoal {
        public ShieldsUpGoal(RiftDemon riftDemon) {
            super(riftDemon, 48, 30,
                    d -> d.random.nextInt(20) == 0 && d.couldBlock(),
                    riftDemon::setShieldUp
            );
        }

        @Override
        public void start() {
            super.start();
            riftDemon.playSound(ModSounds.RIFT_DEMON_SHIELD.get(), 1.2f, 1f);
        }
    }

    public static class GlareGoal extends AnimatedActionGoal {
        public GlareGoal(RiftDemon riftDemon) {
            super(riftDemon, 94, 200,
                    d -> d.random.nextInt(80) == 0 && d.checkGlareDist(),
                    riftDemon::setGlaring
            );
        }

        @Override
        public void start() {
            super.start();
            riftDemon.playSound(ModSounds.RIFT_DEMON_LIGHTNING.get());
        }

        @Override
        public void tick() {
            super.tick();

            switch (tickCounter % 20) {
                case 4 -> addLightning(riftDemon, true, Vec3.ZERO);
                case 0 -> {
                    Vec3 v = target.getViewVector(1f);
                    Vec3 v2 = new Vec3(v.x, 0, v.y);
                    addLightning(target, false, new Vec3(target.getRandom().nextDouble() * 6.0 - 3.0, 0.0, target.getRandom().nextDouble() * 6.0 - 3.0).add(v2));
                }
            }
        }

        private void addLightning(LivingEntity entity, boolean visual, Vec3 offset) {
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(entity.level());
            if (lightningbolt != null) {
                lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()).add(offset));
                lightningbolt.setVisualOnly(visual);
                entity.level().addFreshEntity(lightningbolt);
            }
        }
    }
}
