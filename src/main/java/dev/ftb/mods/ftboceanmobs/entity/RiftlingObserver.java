package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class RiftlingObserver extends Monster implements GeoEntity {
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
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ObserverGazeAttackGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));

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
                boolean wasHurt = super.hurt(source, amount);
                if (!this.level().isClientSide() && this.random.nextInt(7) != 0) {
                    this.teleport();
                }
                return wasHurt;
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
        int y1 = level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z) + 2;

        boolean okToTeleport = false;
        BlockPos blockpos = BlockPos.containing(x, y1, z);
        Vec3 dest = null;
        if (level().hasChunkAt(blockpos)) {
            boolean foundSolidBlock = false;

            while (!foundSolidBlock && blockpos.getY() > level().getMinBuildHeight()) {
                BlockPos blockpos1 = blockpos.below();
                BlockState blockstate = level().getBlockState(blockpos1);
                if (blockstate.blocksMotion()) {
                    foundSolidBlock = true;
                } else {
                    y1--;
                    blockpos = blockpos1;
                }
            }

            if (foundSolidBlock) {
                teleportTo(x, y1, z);
                if (level().noCollision(this) && !level().containsAnyLiquid(this.getBoundingBox())) {
                    dest = new Vec3(x, y1, z);
                    okToTeleport = true;
                } else {
                    teleportTo(xo, yo, zo);
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

    boolean isLookingAtMe(LivingEntity player) {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0 - GAZE_MIN_ANGLE && player.hasLineOfSight(this);
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
        public static final int TOTAL_TIME = 30;
        public static final int ATTACK_TIME = 20;

        private final RiftlingObserver observer;
        private int chargeTime;

        public ObserverGazeAttackGoal(RiftlingObserver observer) {
            this.observer = observer;

            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (chargeTime > 0 || observer.level().getGameTime() - observer.lastGazeTime < 60) {
                return false;
            }

            LivingEntity target = observer.getTarget();
            return target != null && target.isAlive() && observer.canAttack(target) && observer.isLookingAtMe(target);
        }

        @Override
        public boolean canContinueToUse() {
            if (chargeTime > TOTAL_TIME) {
                return false;
            }
            LivingEntity target = observer.getTarget();
            return target != null && target.isAlive() && observer.canAttack(target) && observer.getSensing().hasLineOfSight(target);
        }

        @Override
        public void start() {
            chargeTime = -10;
            observer.getNavigation().stop();
            if (observer.getTarget() != null) {
                observer.getLookControl().setLookAt(observer.getTarget(), 45f, 10f);
            }
            observer.getNavigation().stop();
            observer.setSyncedGazeTarget(observer.getTarget());
        }

        @Override
        public void stop() {
            chargeTime = 0;
            observer.setGazeWarmingUp(false);
            observer.setSyncedGazeTarget(null);
        }

        @Override
        public void tick() {
            observer.setSyncedGazeTarget(chargeTime >= 6 && chargeTime <= 20 ? observer.getTarget() : null);
            if (observer.getTarget() != null) {
                observer.getLookControl().setLookAt(observer.getTarget());
            }

            chargeTime += 2;
            if (chargeTime == 0) {
                observer.setGazeWarmingUp(true);
            } else if (chargeTime == ATTACK_TIME) {
                observer.lastGazeTime = observer.level().getGameTime();

                observer.level().playSound(null, observer.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1f, 1f);

                LivingEntity target = observer.getTarget();
                if (target != null && target.isAlive() && observer.isLookingAtMe(target)) {
                    observer.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.HOSTILE, 1f, 1f);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 10));
                }
            }
        }
    }
}
