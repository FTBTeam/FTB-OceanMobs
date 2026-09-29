package dev.ftb.mods.ftboceanmobs.mobai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/** Approach, strike once, then gain separation before the next pass. */
public class FlyingPassAttackGoal extends DelayedMeleeAttackGoal {
    private int retreatUntil;
    private int nextRetreatPathTick;

    public FlyingPassAttackGoal(PathfinderMob mob) {
        super(mob, 3.0, true, MeleeAttackTiming.WINGED);
    }

    @Override
    public void stop() {
        super.stop();
        retreatUntil = 0;
    }

    @Override
    protected void onAttackFinished() {
        retreatUntil = mob.tickCount + 40;
        nextRetreatPathTick = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (retreatUntil > mob.tickCount && isValidTarget(target)) {
            if (mob.distanceToSqr(target) >= 100) {
                retreatUntil = 0;
                mob.getNavigation().stop();
            } else if (mob.tickCount >= nextRetreatPathTick) {
                nextRetreatPathTick = mob.tickCount + 10;
                Vec3 away = mob.position().subtract(target.position()).multiply(1, 0, 1).normalize();
                if (away.lengthSqr() < 0.01) away = mob.getViewVector(1).scale(-1).multiply(1, 0, 1).normalize();
                // Try both sides when the direct escape is obstructed. Navigation checks the route.
                for (int side : new int[] {0, 1, -1}) {
                    Vec3 destination = mob.position().add(away.scale(8)).add(-away.z * side * 5, 3, away.x * side * 5);
                    if (mob.getNavigation().moveTo(destination.x, destination.y, destination.z, 2.0)) break;
                }
            }
            return;
        }
        super.tick();
    }
}
