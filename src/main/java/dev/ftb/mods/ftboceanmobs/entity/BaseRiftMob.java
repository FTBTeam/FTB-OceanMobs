package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.phys.Vec3;
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
        // rift mobs don't care about light levels for spawning purposes
        return true;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    public void playDelayedAttackSound() {
    }

    static class RiftMobMoveControl extends MoveControl {
        private final BaseRiftMob baseRiftMob;

        public RiftMobMoveControl(BaseRiftMob baseRiftMob) {
            super(baseRiftMob);
            this.baseRiftMob = baseRiftMob;
        }

        @Override
        public void tick() {
            if (baseRiftMob.isInWater()) {
                if (operation == MoveControl.Operation.MOVE_TO && !baseRiftMob.getNavigation().isDone()) {
                    Vec3 vec3 = new Vec3(wantedX - baseRiftMob.getX(), wantedY - baseRiftMob.getY(), wantedZ - baseRiftMob.getZ());
                    double yOff = vec3.y;
                    float yRot = (float) (Mth.atan2(vec3.z, vec3.x) * 180.0F / (float) Math.PI) - 90.0F;
                    baseRiftMob.setYRot(rotlerp(baseRiftMob.getYRot(), yRot, 90.0F));
                    baseRiftMob.yBodyRot = baseRiftMob.getYRot();
                    float baseSpeed = (float) (speedModifier * baseRiftMob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    float speed = Mth.lerp(0.125F, baseRiftMob.getSpeed(), baseSpeed);
                    baseRiftMob.setSpeed(speed);
                    double moveX = Math.cos(baseRiftMob.getYRot() * (float) (Math.PI / 180.0));
                    double moveZ = Math.sin(baseRiftMob.getYRot() * (float) (Math.PI / 180.0));
                    double hBob = Math.sin((double) (baseRiftMob.tickCount + baseRiftMob.getId()) * 0.5) * 0.05;
                    double vBob = Math.sin((double) (baseRiftMob.tickCount + baseRiftMob.getId()) * 0.75) * 0.05;
                    baseRiftMob.setDeltaMovement(baseRiftMob.getDeltaMovement().add(hBob * moveX, vBob * (moveZ + moveX) * 0.35 + (double) speed * yOff * 0.25, hBob * moveZ));
                }
            } else {
                super.tick();
            }
        }
    }
}
