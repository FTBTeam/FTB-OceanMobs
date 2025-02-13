package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.mobai.*;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RiftMinotaur extends BaseRiftMob implements IChargingMob, IThrowingMob {
    private static final int THROW_TICKS = 40;
    private static final int PICKUP_TICKS = 10;
    private static final int LAUNCH_TICKS = 26;
    private static final float THROW_CHANCE = 0.03f;

    protected static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(RiftMinotaur.class, EntityDataSerializers.BYTE);

    private static final byte STATE_NONE = 0x00;
    private static final byte STATE_THROW = 0x01;
    private static final byte STATE_WARMUP_CHARGE = 0x02;
    private static final byte STATE_DO_CHARGE = 0x03;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RiftMinotaur(EntityType<? extends RiftMinotaur> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.28F)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ARMOR, 12F)
                .add(Attributes.ARMOR_TOUGHNESS, 8F)
                .add(Attributes.FOLLOW_RANGE, 36F)
                .add(Attributes.STEP_HEIGHT, 1.5F)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5F)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33333333F)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_STATE, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ChargeGoal(this, 1.5f));
        this.goalSelector.addGoal(1, new ThrowBlockGoal(this, THROW_TICKS, PICKUP_TICKS, LAUNCH_TICKS, THROW_CHANCE));
        this.goalSelector.addGoal(2, new DelayedMeleeAttackGoal(this, 1.0, false, 16));

        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 10, state -> state.setAndContinue(state.isMoving() ? DefaultAnimations.WALK : DefaultAnimations.IDLE)));
        controllers.add(new AnimationController<>(this, "Attacking", 5, this::attackState));
    }

    @Override
    protected float getKnockback(Entity attacker, DamageSource damageSource) {
        // extra knockback when charging
        float knockback = super.getKnockback(attacker, damageSource);
        return isSprinting() ? knockback * 2f : knockback;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);

        @SuppressWarnings("deprecation") var data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        // minotaur always holds weapon in left hand
        setLeftHanded(true);

        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);

        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
    }

    @Override
    public int getCurrentSwingDuration() {
        return 24;
    }

    @Override
    protected AABB getAttackBoundingBox() {
        // long arms...
        return super.getAttackBoundingBox().inflate(1.4);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.MINOTAUR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 0.7F;
    }

    private PlayState attackState(AnimationState<RiftMinotaur> state) {
        if (isThrowing()) {
            return state.setAndContinue(DefaultAnimations.ATTACK_THROW);
        } else if (isAboutToCharge()) {
            return state.setAndContinue(DefaultAnimations.ATTACK_POWERUP);
        } else if (isCharging()) {
            return state.setAndContinue(DefaultAnimations.ATTACK_CHARGE);
        } else if (swinging) {
            return state.setAndContinue(DefaultAnimations.ATTACK_SWING);
        }
        return PlayState.STOP;
    }

    private boolean isAboutToCharge() {
        return entityData.get(DATA_STATE) == STATE_WARMUP_CHARGE;
    }

    private boolean isCharging() {
        return entityData.get(DATA_STATE) == STATE_DO_CHARGE;
    }

    private boolean isThrowing() {
        return entityData.get(DATA_STATE) == STATE_THROW;
    }

    @Override
    public void setWarmingUp() {
        entityData.set(DATA_STATE, STATE_WARMUP_CHARGE);
    }

    @Override
    public void setActuallyCharging() {
        entityData.set(DATA_STATE, STATE_DO_CHARGE);
    }

    @Override
    public void resetCharging() {
        clearFlags();
    }

    private void clearFlags() {
        entityData.set(DATA_STATE, STATE_NONE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void setThrowing(boolean isThrowing) {
        if (isThrowing) {
            entityData.set(DATA_STATE, STATE_THROW);
        } else {
            clearFlags();
        }
    }

}
