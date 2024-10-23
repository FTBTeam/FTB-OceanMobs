package dev.ftb.mods.ftboceanmobs.mobai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Delays the actual attack damage by a few ticks so it works well with the mob's swinging animation
 */
public class DelayedMeleeAttackGoal extends MeleeAttackGoal {
    private final int delayTicks;

    private final Deque<QueuedAttack> queuedAttacks = new ArrayDeque<>();

    public DelayedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, int delayTicks) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);

        this.delayTicks = delayTicks;
    }

    @Override
    public void tick() {
        super.tick();

        if (!queuedAttacks.isEmpty()) {
            QueuedAttack next = queuedAttacks.peekFirst();
            if (next.when <= mob.tickCount) {
                queuedAttacks.removeFirst();
                if (next.target.isAlive() && mob.isWithinMeleeAttackRange(next.target)) {
                    mob.doHurtTarget(next.target);
                }
            }
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            queuedAttacks.addLast(new QueuedAttack(mob.tickCount + delayTicks, target));
        }
    }

    private record QueuedAttack(long when, LivingEntity target) {
    }
}
