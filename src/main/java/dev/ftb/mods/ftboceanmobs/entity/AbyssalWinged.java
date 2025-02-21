package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.mobai.DelayedMeleeAttackGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalWinged extends BaseRiftMob {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssalWinged(EntityType<? extends AbyssalWinged> entityType, Level level) {
        super(entityType, level);

        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 75)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5F)
                .add(Attributes.ARMOR, 16F)
                .add(Attributes.ARMOR_TOUGHNESS, 6F)
                .add(Attributes.FOLLOW_RANGE, 32F)
                .add(Attributes.GRAVITY, 0.015)
                .add(Attributes.ATTACK_DAMAGE, 9.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new DelayedMeleeAttackGoal(this, 3f, true, 15));
        goalSelector.addGoal(2, new AbyssalWingedWanderGoal(this));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
    }

    @Override
    protected AABB getAttackBoundingBox() {
        // long arms...
        return super.getAttackBoundingBox().inflate(1.4);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.ABYSSAL_WINGED_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ABYSSAL_WINGED_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Default", 5, this::determineAnimationState));
    }

    private PlayState determineAnimationState(AnimationState<AbyssalWinged> state) {
        state.setControllerSpeed(1f);
        if (swinging) {
            state.setAnimation(DefaultAnimations.ATTACK_STRIKE);
            state.setControllerSpeed(2.5f);
        } else if (onGround()) {
            state.setAnimation(DefaultAnimations.REST);
        } else if (state.isMoving()) {
            state.setAnimation(DefaultAnimations.FLY);
        } else {
            state.setAnimation(DefaultAnimations.IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public int getCurrentSwingDuration() {
        return 17;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType == MobSpawnType.NATURAL && level.getFluidState(blockPosition()).is(Tags.Fluids.WATER)) {
            BlockPos pos = blockPosition();
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) + 4;
            moveTo(Vec3.atCenterOf(pos.above(y - pos.getY())));
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public static class AbyssalWingedWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public AbyssalWingedWanderGoal(PathfinderMob mob) {
            super(mob, 1.2f);
            interval = 30;
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 viewVec = this.mob.getViewVector(0.0F);
            Vec3 pos = HoverRandomPos.getPos(this.mob, 8, 3, viewVec.x, viewVec.z, (float) (Math.PI / 2), 3, 1);
            return pos != null ? pos : AirAndWaterRandomPos.getPos(this.mob, 8, 3, -2, viewVec.x, viewVec.z, (float) (Math.PI / 2));
        }
    }
}
