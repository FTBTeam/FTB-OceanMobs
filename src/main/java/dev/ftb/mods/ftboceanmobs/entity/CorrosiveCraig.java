package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CorrosiveCraig extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final EntityDataAccessor<Boolean> REGEN = SynchedEntityData.defineId(CorrosiveCraig.class, EntityDataSerializers.BOOLEAN);

    public static final RawAnimation REGEN_ANIMATION = RawAnimation.begin().thenPlay("misc.regeneration");

    private int regenTimer = 0;

    public CorrosiveCraig(EntityType<? extends CorrosiveCraig> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(REGEN, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CraigAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getCurrentSwingDuration() {
        return 25;
    }

    @Override
    protected AABB getAttackBoundingBox() {
        // long arms...
        return super.getAttackBoundingBox().inflate(1.5);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (regenTimer > 0) {
            regenTimer--;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 10, this::walkIdleRegenState));
        controllers.add(DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE));
    }

    private PlayState walkIdleRegenState(AnimationState<CorrosiveCraig> state) {
        if (regenTimer == 0) {
            if (state.isMoving()) {
                state.setAnimation(DefaultAnimations.WALK);
                state.setControllerSpeed(0.75f);
            } else {
                state.setAnimation(DefaultAnimations.IDLE);
            }
            return PlayState.CONTINUE;
        }
        return state.setAndContinue(REGEN_ANIMATION);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (REGEN.equals(key)) {
            if (isRegenActive()) regenTimer = 50;
            if (regenTimer == 50 && level().isClientSide) {
                RandomSource rnd = getRandom();
                for (int i = 0; i < 20; i++) {
                    Vec3 pos = getEyePosition().add(rnd.nextDouble() - 0.5, rnd.nextDouble() + 0.5, rnd.nextDouble() - 0.5);
                    level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.2F, 1F, 0.2F), pos.x, pos.y, pos.z, 0, 0, 0);
                }
            }
        }
    }

    public boolean isRegenActive() {
        return entityData.get(REGEN);
    }

    @Override
    public void onDamageTaken(DamageContainer damageContainer) {
        float prevHealth = getHealth() + damageContainer.getNewDamage();
        if (prevHealth >= getMaxHealth() / 2f && getHealth() < getMaxHealth() / 2f) {
            stopInPlace();
            addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2));
            entityData.set(REGEN, true);
        } else {
            entityData.set(REGEN, false);
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            if (entity.getRandom().nextBoolean()) {
                entity.igniteForTicks(20 + entity.getRandom().nextInt(20));
            }
            return true;
        }
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    class CraigAttackGoal extends MeleeAttackGoal {
        public CraigAttackGoal(CorrosiveCraig mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return CorrosiveCraig.this.regenTimer == 0 && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return CorrosiveCraig.this.regenTimer == 0 && super.canContinueToUse();
        }
    }
}
