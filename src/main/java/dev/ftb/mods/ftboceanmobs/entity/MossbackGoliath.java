package dev.ftb.mods.ftboceanmobs.entity;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import dev.ftb.mods.ftboceanmobs.mobai.RangedPositionGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import dev.ftb.mods.ftboceanmobs.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class MossbackGoliath extends BaseRiftMob {
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
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
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
        goalSelector.addGoal(1, new ShardAttackGoal(this));
        goalSelector.addGoal(2, new RangedPositionGoal(this, 1.3, 8, 16));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController());
        controllers.add(new AnimationController<>("Attacking", 0, this::attackState));
    }

    private PlayState attackState(AnimationTest<MossbackGoliath> state) {
        return getEntityData().get(DATA_SHARD_WARMUP) ? state.setAndContinue(DefaultAnimations.ATTACK_SHOOT) : PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public float getHeadTrackingWeight() {
        return entityData.get(DATA_SHARD_WARMUP) ? 0f : super.getHeadTrackingWeight();
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
        } else if (this.level().isClientSide()) {
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

        if (level().isClientSide()) {
            if (DATA_SHARD_FIRING.equals(key) && entityData.get(DATA_SHARD_FIRING) && getSyncedTarget() != null) {
                MiscUtil.doParticleSpray(this, getSyncedTarget(), ModParticleTypes.MOSSBACK_SHARD.get(), 15);
            } else if (DATA_ATTACK_TARGET.equals(key)) {
                clientSideCachedAttackTarget = null;
            }
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOSSBACK_GOLIATH_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.AMETHYST_BLOCK_STEP);
    }

    private static class ShardAttackGoal extends Goal {
        private static final int SHARD_ANIMATION_TICKS = 35;
        private static final int SHARD_RELEASE_TICK = 15;
        private static final int SHARD_IMPACT_TICK = 23;

        private final MossbackGoliath mossback;
        private LivingEntity attackTarget;
        private int attackStarted;

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
                    // Give retreating a chance, but still fire if cornered.
                    && (mossback.distanceToSqr(target) >= 36 || mossback.tickCount >= mossback.nextShardTick + 40)
                    && mossback.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mossback.getTarget();
            return mossback.shardWarmupTicks > 0 && target != null && target == attackTarget
                    && target.isAlive() && mossback.canAttack(target) && mossback.distanceToSqr(target) < 400
                    && mossback.getSensing().hasLineOfSight(target);
        }

        @Override
        public void stop() {
            mossback.getEntityData().set(DATA_SHARD_WARMUP, false);
            mossback.getEntityData().set(DATA_SHARD_FIRING, false);
            mossback.shardWarmupTicks = 0;
            mossback.setSyncedTarget(null);
            attackTarget = null;
        }

        @Override
        public void start() {
            mossback.getEntityData().set(DATA_SHARD_WARMUP, true);
            attackTarget = mossback.getTarget();
            attackStarted = mossback.tickCount;
            mossback.shardWarmupTicks = SHARD_ANIMATION_TICKS;
            mossback.nextShardTick = mossback.tickCount + mossback.level().getRandom().nextInt(80) + 20 + SHARD_ANIMATION_TICKS;
            if (mossback.getTarget() != null) {
                mossback.getNavigation().stop();
                mossback.getLookControl().setLookAt(mossback.getTarget(), 180f, 180f);
            }
            mossback.setSyncedTarget(mossback.getTarget());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            int elapsed = mossback.tickCount - attackStarted;
            mossback.shardWarmupTicks = Math.max(0, SHARD_ANIMATION_TICKS - elapsed);
            LivingEntity target = attackTarget;
            if (target != null && target == mossback.getTarget() && target.isAlive() && mossback.canAttack(target)
                    && mossback.distanceToSqr(target) < 400 && mossback.level() instanceof ServerLevel serverLevel
                    && mossback.getSensing().hasLineOfSight(target)) {
                mossback.lookControl.setLookAt(target, 45f, 45f);
                if (elapsed == SHARD_RELEASE_TICK) {
                    mossback.entityData.set(DATA_SHARD_FIRING, true);
                    mossback.firingCooldown = 4;
                    mossback.playSound(SoundEvents.WITCH_THROW, 1f, 1f);
                } else if (elapsed == SHARD_IMPACT_TICK) {
                    if (target.isBlocking() && target.getItemBySlot(EquipmentSlot.OFFHAND).getItem() instanceof ShieldItem) {
                        target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1f, 1f);
                        target.getOffhandItem().hurtAndBreak(1, target, EquipmentSlot.OFFHAND);
                    } else {
                        if (target.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(mossback), 3)) {
                            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50 + mossback.getRandom().nextInt(40)));
                        }
                    }
                }
            }
        }
    }
}
