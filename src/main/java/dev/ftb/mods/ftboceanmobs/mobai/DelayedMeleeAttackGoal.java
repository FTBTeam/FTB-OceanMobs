package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.entity.BaseRiftMob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** One committed swing at a time, with server-authoritative impact and recovery. */
public class DelayedMeleeAttackGoal extends MeleeAttackGoal {
    private final MeleeAttackTiming timing;
    private LivingEntity attackTarget;
    private int attackStarted;
    private int nextAttackTick;
    private boolean impactDone;

    public DelayedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, MeleeAttackTiming timing) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.timing = timing;
    }

    protected boolean isValidTarget(LivingEntity target) {
        return target != null && target.isAlive() && mob.canAttack(target)
                && mob.isWithinHome(target.blockPosition());
    }

    @Override
    public boolean canUse() {
        return isValidTarget(mob.getTarget()) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget(mob.getTarget())
                && (isAttackInProgress() || mob.isWithinMeleeAttackRange(mob.getTarget()) || super.canContinueToUse());
    }

    @Override
    public void stop() {
        super.stop();
        cancelAttack();
    }

    protected final boolean isAttackInProgress() {
        return attackTarget != null;
    }

    private void cancelAttack() {
        attackTarget = null;
        mob.swinging = false;
        mob.swingTime = 0;
        // nextAttackTick deliberately survives interruption/restart.
    }

    @Override
    public void tick() {
        if (isAttackInProgress()) {
            if (mob.getTarget() != attackTarget || !isValidTarget(attackTarget)) {
                cancelAttack();
                return;
            }
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(attackTarget, 30f, 30f);
            int elapsed = mob.tickCount - attackStarted;
            if (!impactDone && elapsed >= timing.impactTick()) {
                impactDone = true;
                if (mob instanceof BaseRiftMob riftMob) riftMob.playDelayedAttackSound();
                if (mob.getSensing().hasLineOfSight(attackTarget) && mob.isWithinMeleeAttackRange(attackTarget)) {
                    mob.doHurtTarget(getServerLevel(mob), attackTarget);
                }
            }
            if (elapsed >= timing.durationTicks()) {
                attackTarget = null;
                onAttackFinished();
            }
        } else {
            super.tick();
        }
    }

    protected void onAttackFinished() {
    }

    @Override
    protected boolean isTimeToAttack() {
        return !isAttackInProgress() && mob.tickCount >= nextAttackTick;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (isValidTarget(target) && canPerformAttack(target)) {
            attackTarget = target;
            attackStarted = mob.tickCount;
            nextAttackTick = attackStarted + timing.cooldownTicks();
            impactDone = false;
            mob.getNavigation().stop();
            mob.swing(InteractionHand.MAIN_HAND);
        }
    }
}
