package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import dev.ftb.mods.ftboceanmobs.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class RiftlingObserver extends BaseRiftMob {
    public static final double GAZE_MIN_ANGLE = 0.4;
    private static final int TELEPORT_TIME = 32; // ticks

    private static final RawAnimation TELEPORT_ANIMATION = RawAnimation.begin().thenPlay("move.teleport");
    public static final RawAnimation ATTACK_GAZE = RawAnimation.begin().thenPlay("attack.gaze");

    protected static final EntityDataAccessor<Boolean> DATA_GAZE_WARMING_UP = SynchedEntityData.defineId(RiftlingObserver.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> DATA_TELEPORTING = SynchedEntityData.defineId(RiftlingObserver.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET = SynchedEntityData.defineId(RiftlingObserver.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private long lastGazeTime = 0L;
    @Nullable
    private LivingEntity clientSideCachedAttackTarget;
    private int clientSideGazeWarmupTime;
    private Vec3 pendingTeleportDest;
    private int teleportTimer;

    public RiftlingObserver(EntityType<? extends RiftlingObserver> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.FOLLOW_RANGE, 42.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19F)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ObserverGazeAttackGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_GAZE_WARMING_UP, false);
        builder.define(DATA_TELEPORTING, false);
        builder.define(DATA_ATTACK_TARGET, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide) {
            if (isGazeWarmingUp() && clientSideGazeWarmupTime < ObserverGazeAttackGoal.TOTAL_TIME) {
                clientSideGazeWarmupTime++;
            }
        } else {
            if (isTeleporting()) {
                teleportTimer++;
                if (teleportTimer == 16) {
                    teleportTo(pendingTeleportDest.x, pendingTeleportDest.y, pendingTeleportDest.z);
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                } else if (teleportTimer >= TELEPORT_TIME) {
                    setTeleporting(false);
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RIFTLING_OBSERVER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFTLING_OBSERVER_DEATH.get();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (DATA_ATTACK_TARGET.equals(key)) {
            this.clientSideGazeWarmupTime = 0;
            this.clientSideCachedAttackTarget = null;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            boolean potion = source.getDirectEntity() instanceof ThrownPotion;
            if (!source.is(DamageTypeTags.IS_PROJECTILE) && !potion) {
                if (!this.level().isClientSide() && this.random.nextInt(3) == 0) {
                    this.teleport();
                    return false;
                }
                return super.hurt(source, amount);
            } else {
                for (int i = 0; i < 64; i++) {
                    if (this.teleport()) {
                        return true;
                    }
                }
                return potion;
            }
        }
    }

    private void startTeleporting(Vec3 dest) {
        pendingTeleportDest = dest;
        teleportTimer = 0;
        setTeleporting(true);
    }

    private Vec3 findValidTeleportDest(double x, double y, double z) {
        boolean okToTeleport = false;
        BlockPos blockpos = BlockPos.containing(x, y, z);
        Vec3 dest = null;
        if (level().hasChunkAt(blockpos)) {
            boolean foundValidBlock = false;

            while (!foundValidBlock && blockpos.getY() > level().getMinBuildHeight()) {
                BlockPos blockpos1 = blockpos.below();
                BlockState blockstate = level().getBlockState(blockpos1);
                if (blockstate.blocksMotion() || blockstate.getBlock() instanceof LiquidBlock) {
                    foundValidBlock = true;
                } else {
                    y--;
                    blockpos = blockpos1;
                }
            }

            if (foundValidBlock) {
                if (level().noCollision(this, getBoundingBox().move(x - getX(), y - getY(), z - getZ()))) {
                    dest = new Vec3(x, y, z);
                    okToTeleport = true;
                }
            }
        }

        if (okToTeleport) {
            getNavigation().stop();
            return dest;
        } else {
            return null;
        }
    }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double y = this.getY() + (double)(this.random.nextInt(16) - 8);
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            Vec3 dest = findValidTeleportDest(x, y, z);
            if (dest != null) {
                startTeleporting(dest);
                return true;
            }
        }
        return false;
    }

    private void setTeleporting(boolean isTeleporting) {
        entityData.set(DATA_TELEPORTING, isTeleporting);
    }

    public boolean isTeleporting() {
        return entityData.get(DATA_TELEPORTING);
    }

    private void setGazeWarmingUp(boolean warmingUp) {
        getEntityData().set(DATA_GAZE_WARMING_UP, warmingUp);
    }

    public boolean isGazeWarmingUp() {
        return getEntityData().get(DATA_GAZE_WARMING_UP);
    }

    public boolean hasSyncedGazeTarget() {
        return entityData.get(DATA_ATTACK_TARGET) != 0;
    }

    public void setSyncedGazeTarget(@Nullable LivingEntity target) {
        entityData.set(DATA_ATTACK_TARGET, target == null ? 0 : target.getId());
    }

    @Nullable
    public LivingEntity getSyncedGazeTarget() {
        if (!this.hasSyncedGazeTarget()) {
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(DefaultAnimations.genericAttackAnimation(this, ATTACK_GAZE));
        controllers.add(new AnimationController<>(this, "Teleport", 32, this::teleportAnimationState));
        controllers.add(new AnimationController<>(this, "Gaze", 32, this::gazeAnimationState));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public int getCurrentSwingDuration() {
        return 30;
    }

    public float getAttackAnimationScale(float partialTick) {
        return ((float)this.clientSideGazeWarmupTime + partialTick) / ObserverGazeAttackGoal.TOTAL_TIME;
    }

    public float getClientSideAttackTime() {
        return clientSideGazeWarmupTime;
    }

    private PlayState teleportAnimationState(AnimationState<RiftlingObserver> state) {
        if (isTeleporting()) {
            state.setAnimation(TELEPORT_ANIMATION);
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    private PlayState gazeAnimationState(AnimationState<RiftlingObserver> state) {
        if (isGazeWarmingUp()) {
            state.setAnimation(ATTACK_GAZE);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private static class ObserverGazeAttackGoal extends Goal {
        public static final int TOTAL_TIME = reducedTickDelay(30);
        public static final int ATTACK_TIME = reducedTickDelay(20);

        private final RiftlingObserver observer;
        private int chargeTime;
        private LivingEntity target;

        public ObserverGazeAttackGoal(RiftlingObserver observer) {
            this.observer = observer;

            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (chargeTime > 0 || observer.level().getGameTime() - observer.lastGazeTime < 60) {
                return false;
            }

            target = observer.getTarget();
            return target != null && target.isAlive() && observer.canAttack(target) && MiscUtil.isLookingAtMe(observer, target, GAZE_MIN_ANGLE);
        }

        @Override
        public boolean canContinueToUse() {
            return chargeTime < TOTAL_TIME && target.isAlive() && observer.canAttack(target) && observer.getSensing().hasLineOfSight(target);
        }

        @Override
        public void start() {
            chargeTime = -5;
            observer.getNavigation().stop();
            observer.getLookControl().setLookAt(target, 45f, 10f);
            observer.setSyncedGazeTarget(observer.getTarget());
        }

        @Override
        public void stop() {
            chargeTime = 0;
            observer.setGazeWarmingUp(false);
            observer.setSyncedGazeTarget(null);
            target = null;
        }

        @Override
        public void tick() {
            observer.setSyncedGazeTarget(chargeTime >= 3 && chargeTime <= 10 ? target : null);
            observer.getLookControl().setLookAt(target);

            chargeTime++;
            if (chargeTime == 0) {
                observer.setGazeWarmingUp(true);
            } else if (chargeTime == ATTACK_TIME) {
                observer.lastGazeTime = observer.level().getGameTime();

                observer.playSound(SoundEvents.EVOKER_CAST_SPELL, 1f, 1f);

                if (target != null && target.isAlive() && MiscUtil.isLookingAtMe(observer, target, GAZE_MIN_ANGLE)) {
                    observer.level().playSound(null, target.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1f, 1f);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 10));
                }
            }
        }
    }
}
