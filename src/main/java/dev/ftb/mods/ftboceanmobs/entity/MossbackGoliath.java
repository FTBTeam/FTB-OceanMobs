package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MossbackGoliath extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int nextShardTick;
    private int shardWarmupTicks;
    private int firingCooldown;

    protected static final EntityDataAccessor<Boolean> DATA_SHARD_WARMUP = SynchedEntityData.defineId(MossbackGoliath.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> DATA_SHARD_FIRING = SynchedEntityData.defineId(MossbackGoliath.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET = SynchedEntityData.defineId(MossbackGoliath.class, EntityDataSerializers.INT);
    private LivingEntity clientSideCachedAttackTarget;

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.28F)
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ARMOR, 8F)
                .add(Attributes.ARMOR_TOUGHNESS, 6F)
                .add(Attributes.FOLLOW_RANGE, 36F)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    public MossbackGoliath(EntityType<? extends MossbackGoliath> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_SHARD_WARMUP, false);
        builder.define(DATA_SHARD_FIRING, false);
        builder.define(DATA_ATTACK_TARGET, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ShardAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.3, 32.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(new AnimationController<>(this, "Attacking", 5, this::attackState));
    }

    private PlayState attackState(AnimationState<MossbackGoliath> state) {
        return getEntityData().get(DATA_SHARD_WARMUP) ? state.setAndContinue(DefaultAnimations.ATTACK_SHOOT) : PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public int getCurrentSwingDuration() {
        return 35;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            if (entityData.get(DATA_SHARD_FIRING) && --firingCooldown <= 0) {
                entityData.set(DATA_SHARD_FIRING, false);
            }
        }
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

        if (level().isClientSide)
            if (DATA_SHARD_FIRING.equals(key) && entityData.get(DATA_SHARD_FIRING) && getSyncedTarget() != null) {
                // play particles
                Vec3 start = getEyePosition().add(getLookAngle().normalize().scale(0.5));
                Vec3 vel = getSyncedTarget().getEyePosition().subtract(start).normalize();
                for (int i = 0; i < 15; i++) {
                    Vec3 vel2 = vel.add(random.nextDouble() * 0.4 - 0.2, random.nextDouble() * 0.4 - 0.2, random.nextDouble() * 0.4 - 0.2);
                    level().addParticle(ModParticleTypes.MOSSBACK_SHARD.get(), start.x, start.y, start.z, vel2.x, vel2.y, vel2.z);
                }
            } else if (DATA_ATTACK_TARGET.equals(key)) {
                clientSideCachedAttackTarget = null;
            }
    }

    private static class ShardAttackGoal extends Goal {
        private static final int SHARD_WARMUP_TICKS = 35;

        private final MossbackGoliath mossback;

        public ShardAttackGoal(MossbackGoliath mossback) {
            this.mossback = mossback;

            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mossback.getTarget();
            return target != null && target.isAlive()
                    && mossback.canAttack(target)
                    && mossback.tickCount >= mossback.nextShardTick
                    && mossback.distanceToSqr(target) < 400
                    && mossback.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mossback.getTarget();
            return mossback.shardWarmupTicks > 0 && target != null && target.isAlive() && mossback.canAttack(target);
        }

        @Override
        public void stop() {
            mossback.getEntityData().set(DATA_SHARD_WARMUP, false);
            mossback.setSyncedTarget(null);
        }

        @Override
        public void start() {
            mossback.getEntityData().set(DATA_SHARD_WARMUP, true);
            mossback.shardWarmupTicks = adjustedTickDelay(SHARD_WARMUP_TICKS);
            mossback.nextShardTick = mossback.tickCount + mossback.level().random.nextInt(80) + 20 + SHARD_WARMUP_TICKS;
            if (mossback.getTarget() != null) {
                mossback.getNavigation().stop();
                mossback.getLookControl().setLookAt(mossback.getTarget(), 180f, 180f);
            }
            mossback.setSyncedTarget(mossback.getTarget());
        }

        @Override
        public void tick() {
            LivingEntity target = mossback.getTarget();
            if (target != null && mossback.level() instanceof ServerLevel serverLevel && mossback.getSensing().hasLineOfSight(target)) {
                mossback.lookControl.setLookAt(target, 45f, 45f);
                --mossback.shardWarmupTicks;
                if (mossback.shardWarmupTicks == 8) {
                    mossback.entityData.set(DATA_SHARD_FIRING, true);
                    mossback.firingCooldown = 4;
                } else if (mossback.shardWarmupTicks == 0) {
                    if (target.isBlocking() && target.getItemBySlot(EquipmentSlot.OFFHAND).getItem() instanceof ShieldItem) {
                        target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1f, 1f);
                        target.getOffhandItem().hurtAndBreak(1, target, EquipmentSlot.OFFHAND);
                    } else {
                        target.hurt(serverLevel.damageSources().mobAttack(mossback), 3);
                        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40 + mossback.getRandom().nextInt(40)));
                    }
                }
            }
        }
    }
}
