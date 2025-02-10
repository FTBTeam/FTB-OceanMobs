package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import software.bernie.geckolib.animatable.GeoEntity;

public abstract class BaseRiftMob extends Monster implements GeoEntity {
    protected BaseRiftMob(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        this.moveControl = new RiftMobMoveControl(this);
        setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        // all rift mobs can spawn in water
        return level.isUnobstructed(this);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return true;
    }

    protected boolean wantsToSwim() {
        LivingEntity target = getTarget();
        return target != null && (target.isInWater() || target.getY() - getY() > 2.0);
    }

    static class RiftMobMoveControl extends MoveControl {
        private final BaseRiftMob baseRiftMob;

        public RiftMobMoveControl(BaseRiftMob baseRiftMob) {
            super(baseRiftMob);
            this.baseRiftMob = baseRiftMob;
        }

        @Override
        public void tick() {
            LivingEntity livingentity = baseRiftMob.getTarget();
            if (baseRiftMob.wantsToSwim() && baseRiftMob.isInWater()) {
                if (livingentity != null && livingentity.getY() > baseRiftMob.getY()) {
                    baseRiftMob.setDeltaMovement(baseRiftMob.getDeltaMovement().add(0.0, 0.03, 0.0));
                }

                if (this.operation != MoveControl.Operation.MOVE_TO || baseRiftMob.getNavigation().isDone()) {
                    baseRiftMob.setSpeed(0.0F);
                    return;
                }

                double xOff = this.wantedX - baseRiftMob.getX();
                double yOff = this.wantedY - baseRiftMob.getY();
                double zOff = this.wantedZ - baseRiftMob.getZ();
//                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
//                d1 /= d3;
                float f = (float)(Mth.atan2(zOff, xOff) * 180.0F / (float)Math.PI) - 90.0F;
                baseRiftMob.setYRot(this.rotlerp(baseRiftMob.getYRot(), f, 90.0F));
                baseRiftMob.yBodyRot = baseRiftMob.getYRot();
                float baseSpeed = (float)(this.speedModifier * baseRiftMob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float speed = Mth.lerp(0.125F, baseRiftMob.getSpeed(), baseSpeed);
                baseRiftMob.setSpeed(speed);
                baseRiftMob.setDeltaMovement(baseRiftMob.getDeltaMovement().add((double)speed * xOff * 0.005, (double)speed * yOff * 0.1, (double)speed * zOff * 0.005));
            } else {
//                if (!baseRiftMob.onGround()) {
//                    baseRiftMob.setDeltaMovement(baseRiftMob.getDeltaMovement().add(0.0, -0.008, 0.0));
//                }

                super.tick();
            }
        }
    }
}
