package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.mobai.DelayedMeleeAttackGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CorrosiveCraig extends BaseRiftMob {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final EntityDataAccessor<Boolean> REGEN = SynchedEntityData.defineId(CorrosiveCraig.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FIRE_FIST = SynchedEntityData.defineId(CorrosiveCraig.class, EntityDataSerializers.BOOLEAN);

    public static final RawAnimation REGEN_ANIMATION = RawAnimation.begin().thenPlay("misc.regeneration");

    private int regenTimer = 0;

    public CorrosiveCraig(EntityType<? extends CorrosiveCraig> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(REGEN, false);
        builder.define(FIRE_FIST, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.ARMOR, 15F)
                .add(Attributes.ARMOR_TOUGHNESS, 10F)
                .add(Attributes.FOLLOW_RANGE, 36F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 15.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CraigAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));

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
        return super.getAttackBoundingBox().inflate(1.9);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (regenTimer > 0) {
            regenTimer--;
        }

        if (regenTimer == 30 && level().isClientSide) {
            RandomSource rnd = getRandom();
            for (int i = 0; i < 50; i++) {
                Vec3 pos = getEyePosition().add(rnd.nextDouble() - 0.5, rnd.nextDouble() + 0.5, rnd.nextDouble() - 0.5);
                level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.2F, 1F, 0.2F), pos.x, pos.y, pos.z, 0, 0, 0);
            }
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
                state.setControllerSpeed(0.57f);
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
            if (entityData.get(FIRE_FIST)) {
                entity.igniteForTicks(40 + entity.getRandom().nextInt(40));
            }
            return true;
        }
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.2F, 0.75F);
    }

    @Override
    public void playDelayedAttackSound() {
        playSound(ModSounds.CORROSIVE_CRAIG_ATTACK.get(), 1f, 0.8f + random.nextFloat() * 0.4f);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CORROSIVE_CRAIG_DEATH.get();
    }

    protected float nextStep() {
        return moveDist + 0.55f;
    }

    class CraigAttackGoal extends DelayedMeleeAttackGoal {
        public CraigAttackGoal(CorrosiveCraig mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen, 20);
        }

        @Override
        public boolean canUse() {
            return CorrosiveCraig.this.regenTimer == 0 && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return CorrosiveCraig.this.regenTimer == 0 && super.canContinueToUse();
        }

        @Override
        public void stop() {
            super.stop();

            CorrosiveCraig.this.entityData.set(FIRE_FIST, false);
        }

        @Override
        public void tick() {
            super.tick();

            if (mob.getRandom().nextInt(30) == 0) {
                CorrosiveCraig.this.entityData.set(FIRE_FIST, !CorrosiveCraig.this.entityData.get(FIRE_FIST));
            }
        }
    }
}
