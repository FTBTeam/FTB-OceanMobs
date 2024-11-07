package dev.ftb.mods.ftboceanmobs.mobai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Like MoveTowardTargetGoal, but much more aggressively, and follows target until close enough.
 */
public class ChaseTargetGoal extends Goal {
    private final PathfinderMob mob;
    @Nullable
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;
    private int ticksUntilPathRecalc;

    public ChaseTargetGoal(PathfinderMob mob, double speedModifier, float within) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.within = within;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        if (target == null) {
            return false;
        } else if (target.distanceToSqr(mob) > (double)(within * within)) {
            return false;
        } else if (target.distanceToSqr(mob) < 49.0) {
            return false;
        } else {
            wantedX = target.getX();
            wantedY = target.getY();
            wantedZ = target.getZ();
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && target.isAlive() && target.distanceToSqr(mob) < (double)(within * within);
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(wantedX, wantedY, wantedZ, 7, speedModifier);
        ticksUntilPathRecalc = 0;
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(target);
        if (ticksUntilPathRecalc == 0) {
            wantedX = target.getX();
            wantedY = target.getY();
            wantedZ = target.getZ();
            boolean pathOk = mob.getNavigation().moveTo(wantedX, wantedY, wantedZ, 1, speedModifier);
            ticksUntilPathRecalc = 4 + mob.getRandom().nextInt(7) + (pathOk ? 0 : 15);
        }
    }
}
