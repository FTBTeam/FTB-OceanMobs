package dev.ftb.mods.ftboceanmobs.mobai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Maintain firing room and seek a new angle when sight is blocked. */
public class RangedPositionGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final double minRangeSq;
    private final double maxRangeSq;
    private int nextPathTick;
    private boolean retreating;

    public RangedPositionGoal(PathfinderMob mob, double speed, double minRange, double maxRange) {
        this.mob = mob;
        this.speed = speed;
        minRangeSq = minRange * minRange;
        maxRangeSq = maxRange * maxRange;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.canAttack(target) && mob.isWithinHome(target.blockPosition());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        nextPathTick = 0;
        retreating = false;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.getLookControl().setLookAt(target, 30f, 30f);
        double distance = mob.distanceToSqr(target);
        boolean visible = mob.getSensing().hasLineOfSight(target);
        // Hysteresis lets a retreat finish instead of jittering at the minimum range.
        retreating = distance < minRangeSq || retreating && distance < minRangeSq * 1.5;
        if (!retreating && visible && distance <= maxRangeSq) {
            mob.getNavigation().stop();
            return;
        }
        if (mob.tickCount < nextPathTick) return;
        nextPathTick = mob.tickCount + 10;
        if (retreating) {
            Vec3 pos = DefaultRandomPos.getPosAway(mob, 8, 4, target.position());
            if (pos != null && pos.distanceToSqr(target.position()) > distance) {
                mob.getNavigation().moveTo(pos.x, pos.y, pos.z, speed);
            }
        } else if (!visible && distance <= maxRangeSq) {
            Vec3 pos = DefaultRandomPos.getPosTowards(mob, 8, 4, target.position(), Math.PI / 2);
            if (pos == null || !mob.getNavigation().moveTo(pos.x, pos.y, pos.z, speed)) {
                mob.getNavigation().moveTo(target, speed);
            }
        } else {
            mob.getNavigation().moveTo(target, speed);
        }
    }
}
