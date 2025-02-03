package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalSludge extends BaseRiftMob {
    private static final RawAnimation ANIM_ATTACK_SPLASH = RawAnimation.begin().thenPlay("attack.splash");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final EntityDataAccessor<Boolean> DATA_SLUDGE_WARMUP = SynchedEntityData.defineId(AbyssalSludge.class, EntityDataSerializers.BOOLEAN);

    private int sludgeWarmupTicks = 0;
    private int nextSludgeTick = 0;

    public AbyssalSludge(EntityType<? extends AbyssalSludge> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.MAX_HEALTH, 75.0)
                .add(Attributes.ARMOR, 8F)
                .add(Attributes.ARMOR_TOUGHNESS, 6F)
                .add(Attributes.FOLLOW_RANGE, 42F)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 9.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_SLUDGE_WARMUP, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ThrowSludgeGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(new AnimationController<>(this, "Attacking", 5, this::attackState));
    }

    private PlayState attackState(AnimationState<AbyssalSludge> state) {
        state.setControllerSpeed(1f);
        if (swinging) {
            state.setControllerSpeed(2f);
            return state.setAndContinue(DefaultAnimations.ATTACK_STRIKE);
        } else if (getEntityData().get(DATA_SLUDGE_WARMUP)) {
            return state.setAndContinue(ANIM_ATTACK_SPLASH);
        }
        return PlayState.STOP;
    }

    @Override
    public int getCurrentSwingDuration() {
        return entityData.get(DATA_SLUDGE_WARMUP) ? 24 : 15;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && super.doHurtTarget(entity)) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30 + entity.getRandom().nextInt(40), 3));
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
        if (level().random.nextBoolean()) {
            playSound(SoundEvents.SLIME_SQUISH, 1.0F, 0.75F);
        } else {
            playSound(SoundEvents.SLIME_JUMP, 1.0F, 1F);
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    private static class ThrowSludgeGoal extends Goal {
        private static final int SLUDGE_WARMUP_TICKS = 24;
        private static final TargetingConditions SLIME_COUNT_TARGETING
                = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();

        private final AbyssalSludge abyssalSludge;

        public ThrowSludgeGoal(AbyssalSludge abyssalSludge) {
            this.abyssalSludge = abyssalSludge;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = abyssalSludge.getTarget();
            if (target != null && target.isAlive()
                    && abyssalSludge.canAttack(target)
                    && abyssalSludge.distanceToSqr(target) >= 25
                    && abyssalSludge.tickCount >= abyssalSludge.nextSludgeTick)
            {
                int nSlimes = abyssalSludge.level()
                        .getNearbyEntities(Slime.class, SLIME_COUNT_TARGETING, abyssalSludge, abyssalSludge.getBoundingBox().inflate(16.0))
                        .size();
                return nSlimes < 12;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = abyssalSludge.getTarget();
            return abyssalSludge.sludgeWarmupTicks > 0 && target != null && target.isAlive() && abyssalSludge.canAttack(target);
        }

        @Override
        public void start() {
            abyssalSludge.getEntityData().set(DATA_SLUDGE_WARMUP, true);
            abyssalSludge.sludgeWarmupTicks = adjustedTickDelay(SLUDGE_WARMUP_TICKS);
            abyssalSludge.nextSludgeTick = abyssalSludge.tickCount + abyssalSludge.level().random.nextInt(40) + 40 + SLUDGE_WARMUP_TICKS;
        }

        @Override
        public void stop() {
            abyssalSludge.getEntityData().set(DATA_SLUDGE_WARMUP, false);
        }

        @Override
        public void tick() {
            // fire the sludgeling a few ticks before the animation ends; looks better that way
            if (--abyssalSludge.sludgeWarmupTicks == 4 && abyssalSludge.getTarget() != null && abyssalSludge.level() instanceof ServerLevel serverLevel) {
                abyssalSludge.lookControl.setLookAt(abyssalSludge.getTarget());
                Vec3 look = abyssalSludge.getLookAngle().normalize();
                Vec3 pos = abyssalSludge.getEyePosition().add(look.scale(0.5));
                Sludgeling sludgeling = ModEntityTypes.SLUDGELING.get().create(serverLevel);
                if (sludgeling != null) {
                    Vec3 vec = abyssalSludge.getTarget().position().subtract(abyssalSludge.position());
                    sludgeling.setDeltaMovement(vec.scale(0.125));
                    sludgeling.moveTo(pos, 0.0F, 0.0F);
                    sludgeling.setTarget(abyssalSludge.getTarget());
                    sludgeling.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(abyssalSludge.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                    sludgeling.setSize(2, false);
                    sludgeling.setHealth(sludgeling.getMaxHealth());
                    serverLevel.addFreshEntityWithPassengers(sludgeling);
                    serverLevel.gameEvent(GameEvent.ENTITY_PLACE, BlockPos.containing(pos), GameEvent.Context.of(abyssalSludge));
                    abyssalSludge.playSound(SoundEvents.SLIME_BLOCK_HIT, 1f, 0.5f);
                }
            }
        }
    }
}
