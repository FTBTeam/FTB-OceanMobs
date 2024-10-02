package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalWinged extends Monster implements Enemy, GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssalWinged(EntityType<? extends AbyssalWinged> entityType, Level level) {
        super(entityType, level);

        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5F)
                .add(Attributes.ATTACK_DAMAGE, /*3.0*/ 0.1);
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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25f, true));
        goalSelector.addGoal(2, new AbyssalWingedWanderGoal(this));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8F));

        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
