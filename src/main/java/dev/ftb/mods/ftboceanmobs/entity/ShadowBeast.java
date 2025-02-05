package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.mobai.DelayedMeleeAttackGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class ShadowBeast extends BaseRiftMob {
    private static final RawAnimation ANIM_ATTACK_ROAR = RawAnimation.begin().thenPlay("attack.roar");

    protected static final EntityDataAccessor<Boolean> DATA_ROAR_WARMUP = SynchedEntityData.defineId(ShadowBeast.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int nextRoarTick;
    private int roarWarmupTick;

    public ShadowBeast(EntityType<? extends ShadowBeast> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.45F)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ARMOR, 2F)
                .add(Attributes.ARMOR_TOUGHNESS, 4F)
                .add(Attributes.FOLLOW_RANGE, 48F)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_ROAR_WARMUP, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ShadowRoarGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.42F));
        this.goalSelector.addGoal(3, new DelayedMeleeAttackGoal(this, 1.0, false, 12));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 10, this::walkIdleRegenState));
        controllers.add(new AnimationController<>(this, "Attacking", 5, this::attackState));
    }

    private PlayState attackState(AnimationState<ShadowBeast> state) {
        state.setControllerSpeed(1f);
        if (isRoaring()) {
            return state.setAndContinue(ANIM_ATTACK_ROAR);
        } else if (swinging) {
            return state.setAndContinue(DefaultAnimations.ATTACK_STRIKE);
        }
        return PlayState.STOP;
    }

    private PlayState walkIdleRegenState(AnimationState<ShadowBeast> state) {
        state.setControllerSpeed(1f);
        if (state.isMoving()) {
            state.setControllerSpeed(3f);
            state.setAnimation(DefaultAnimations.WALK);
        } else {
            state.setAnimation(DefaultAnimations.IDLE);
        }
        return PlayState.CONTINUE;
    }

    public boolean isRoaring() {
        return entityData.get(DATA_ROAR_WARMUP);
    }

    private void setRoaring(boolean roaring) {
        entityData.set(DATA_ROAR_WARMUP, roaring);
    }

    @Override
    public int getCurrentSwingDuration() {
        return isRoaring() ? 40 : 20;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && super.doHurtTarget(entity)) {
            if (entity.getRandom().nextInt(6) == 0) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40 + entity.getRandom().nextInt(40)));
            }
            return true;
        }
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static class ShadowRoarGoal extends Goal {
        private static final int ROAR_WARMUP_TIME = 40;

        private final ShadowBeast shadowBeast;

        public ShadowRoarGoal(ShadowBeast shadowBeast) {
            this.shadowBeast = shadowBeast;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return shadowBeast.getTarget() != null && shadowBeast.tickCount >= shadowBeast.nextRoarTick
                    && shadowBeast.distanceToSqr(shadowBeast.getTarget()) >= 16;
        }

        @Override
        public boolean canContinueToUse() {
            return shadowBeast.roarWarmupTick >= 0;
        }

        @Override
        public void start() {
            shadowBeast.setRoaring(true);
            shadowBeast.getNavigation().stop();
            if (shadowBeast.getTarget() != null) {
                shadowBeast.getLookControl().setLookAt(shadowBeast.getTarget(), 45f, 10f);
            }
            shadowBeast.roarWarmupTick = adjustedTickDelay(ROAR_WARMUP_TIME);
            shadowBeast.nextRoarTick = shadowBeast.tickCount + shadowBeast.getRandom().nextInt(40) + 40 + ROAR_WARMUP_TIME;
        }

        @Override
        public void stop() {
            shadowBeast.setRoaring(false);
        }

        @Override
        public void tick() {
            shadowBeast.roarWarmupTick--;
            if (shadowBeast.roarWarmupTick == 16) {
                shadowBeast.playSound(SoundEvents.WARDEN_ROAR, 1f,1.5f + shadowBeast.getRandom().nextFloat() * 0.5f);
            } else if (shadowBeast.roarWarmupTick == 10) {
                shadowBeast.level().getNearbyEntities(Player.class, TargetingConditions.DEFAULT, shadowBeast, shadowBeast.getBoundingBox().inflate(16.0))
                        .forEach(player -> {
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 + shadowBeast.getRandom().nextInt(60), 3));
                            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60 + shadowBeast.getRandom().nextInt(20), 0));
                        });
            }
        }
    }

}
