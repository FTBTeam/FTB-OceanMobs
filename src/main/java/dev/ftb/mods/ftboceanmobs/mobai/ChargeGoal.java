package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.integration.ftbchunks.FTBChunksIntegration;
import dev.ftb.mods.ftboceanmobs.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.EnumSet;

public class ChargeGoal extends Goal {
    private static final double MIN_CHARGE_RANGE_SQ = 5.0 * 5.0;
    private static final double MAX_CHARGE_RANGE_SQ = 15.0 * 15.0;
    private static final float CHARGE_CHANCE = 0.12f;
    private static final int WARMUP_TICKS = 40;
    private static final int MAX_CHARGE_TICKS = 40;
    private static final int RECOVERY_TICKS = 20;
    private static final int COOLDOWN_TICKS = 60;

    private enum Phase { WARMUP, CHARGING, RECOVERY }

    private final PathfinderMob mob;
    private final boolean canBreakBlocks;
    private final float speed;

    private LivingEntity target;
    private Vec3 chargePos;
    private Phase phase;
    private int phaseEndTick;
    private int nextChargeTick;
    private boolean chargeSwipeDone;

    public ChargeGoal(PathfinderMob mob, float speed) {
        this.mob = mob;
        this.canBreakBlocks = mob instanceof IChargingMob c && c.canBreakBlocksWhenCharging();
        this.speed = speed;

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.tickCount < nextChargeTick || isStandingInFluid()) {
            return false;
        }
        target = mob.getTarget();

        if (target == null || !target.isAlive() || !mob.canAttack(target)) {
            return false;
        }

        double distSq = mob.distanceToSqr(target);
        if (distSq < MIN_CHARGE_RANGE_SQ || distSq > MAX_CHARGE_RANGE_SQ
                || !mob.onGround()
                || mob.getRandom().nextFloat() > CHARGE_CHANCE
                || !mob.getSensing().hasLineOfSight(target)
                || !MiscUtil.canPathfindToTarget(mob, target, 2.25F)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target == mob.getTarget() && target.isAlive() && mob.canAttack(target)
                && !isStandingInFluid() && (phase != Phase.RECOVERY || mob.tickCount < phaseEndTick);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        phase = Phase.WARMUP;
        phaseEndTick = mob.tickCount + WARMUP_TICKS;
        chargeSwipeDone = false;
        mob.setSprinting(false);
        stopMovement();
        if (mob instanceof IChargingMob c) c.setWarmingUp();
    }

    @Override
    public void stop() {
        stopMovement();
        target = null;
        chargePos = null;
        nextChargeTick = mob.tickCount + COOLDOWN_TICKS;
        mob.setSprinting(false);
        if (mob instanceof IChargingMob c) c.resetCharging();
    }

    private void stopMovement() {
        mob.getNavigation().stop();
        mob.setDeltaMovement(mob.getDeltaMovement().multiply(0, 1, 0));
    }

    private void beginRecovery() {
        phase = Phase.RECOVERY;
        phaseEndTick = mob.tickCount + RECOVERY_TICKS;
        mob.setSprinting(false);
        stopMovement();
        if (mob instanceof IChargingMob c) c.resetCharging();
    }

    @Override
    public void tick() {
        if (phase == Phase.RECOVERY) {
            stopMovement();
            return;
        }
        if (phase == Phase.WARMUP) {
            stopMovement();
            mob.getLookControl().setLookAt(target, 10f, mob.getMaxHeadXRot());
            if (mob.tickCount < phaseEndTick) return;

            // Aim once at launch. The player can now dodge this committed path.
            chargePos = calcChargePos(mob, target);
            phase = Phase.CHARGING;
            phaseEndTick = mob.tickCount + MAX_CHARGE_TICKS;
            mob.setSprinting(true);
            if (mob instanceof IChargingMob c) c.setActuallyCharging();
            if (!mob.getNavigation().moveTo(chargePos.x, chargePos.y, chargePos.z, speed)) {
                beginRecovery();
                return;
            }
        }

        if (mob.tickCount >= phaseEndTick || mob.getNavigation().isDone() || mob.position().distanceToSqr(chargePos) < 1) {
            beginRecovery();
            return;
        }
        mob.getLookControl().setLookAt(chargePos.x, mob.getEyeY(), chargePos.z, 10f, mob.getMaxHeadXRot());

        if (canBreakBlocks && EventHooks.canEntityGrief(getServerLevel(mob), mob)) {
            AABB aabb = mob.getBoundingBox().inflate(0.8, 0.0, 0.8).move(0.0, 1.1, 0.0);
            BlockPos min = BlockPos.containing(aabb.getMinPosition());
            BlockPos max = BlockPos.containing(aabb.getMaxPosition());
            if (mob.level().hasChunksAt(min, max)) {
                for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                    if (canDestroyBlock(mob.level(), pos, mob)) {
                        mob.level().destroyBlock(pos, true);
                    }
                }
            }
        }

        double rangeSq = mob.getBbWidth() * 2.0F * mob.getBbWidth() * 2.0F + target.getBbWidth();
        if (mob.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= rangeSq) {
            if (!chargeSwipeDone && mob.getSensing().hasLineOfSight(target)) {
                mob.doHurtTarget(getServerLevel(mob), target);
                chargeSwipeDone = true;
            }
        }
    }

    private boolean isStandingInFluid() {
        return mob.level().getBlockState(mob.getOnPos().above()).getBlock() instanceof LiquidBlock;
    }

    private static Vec3 calcChargePos(PathfinderMob mob, LivingEntity target) {
        Vec3 offset = target.position().subtract(mob.position());
        // should send the charger 2.5 blocks past the target's position
        return mob.position().add(offset.add(offset.normalize().scale(2.5)));
    }

    private static boolean canDestroyBlock(Level level, BlockPos pos, LivingEntity entity) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0f && hardness < 50f && !state.isAir()
                && level.getBlockEntity(pos) == null
                && state.getBlock().canEntityDestroy(state, level, pos, entity)
                && FTBChunksIntegration.canMobGriefBlocks(level, pos)
                && EventHooks.onEntityDestroyBlock(entity, pos, state);
    }
}
