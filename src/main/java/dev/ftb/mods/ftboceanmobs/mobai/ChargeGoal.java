package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.MiscUtil;
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

    private final PathfinderMob mob;
    private final boolean canBreakBlocks;
    private final float speed;

    private LivingEntity target;
    private Vec3 chargePos;
    private int chargeWarmup;
    private boolean chargeSwipeDone;

    public ChargeGoal(PathfinderMob mob, float speed) {
        this.mob = mob;
        this.canBreakBlocks = mob instanceof IChargingMob c && c.canBreakBlocksWhenCharging();
        this.speed = speed;

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (isStandingInFluid()) {
            return false;
        }
        target = mob.getTarget();

        if (target == null) {
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
        chargePos = calcChargePos(mob, target);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        boolean b = target.isAlive() && mob.canAttack(target)
                && !isStandingInFluid()
                && (chargeWarmup > 0 || !mob.getNavigation().isDone());
        return b;
    }

    @Override
    public void start() {
        chargeWarmup = adjustedTickDelay(WARMUP_TICKS);
        mob.setSprinting(true);
        if (mob instanceof IChargingMob c) {
            c.setWarmingUp();
        }
    }

    @Override
    public void stop() {
        chargeWarmup = 0;
        target = null;
        chargeSwipeDone = false;
        mob.setSprinting(false);
        if (mob instanceof IChargingMob c) {
            c.resetCharging();
        }
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(chargePos.x(), chargePos.y() - 1, chargePos.z(), 10.0F, mob.getMaxHeadXRot());

        if (--chargeWarmup > 0) {
            // warming up...
            mob.walkAnimation.setSpeed(mob.walkAnimation.speed() + 0.8f);
        } else {
            // charge!
            if (mob instanceof IChargingMob c) {
                c.setActuallyCharging();
            }
            // recalc here since player has had plenty time to move during warmup...
            chargePos = calcChargePos(mob, target);
            mob.getNavigation().moveTo(chargePos.x(), chargePos.y(), chargePos.z(), speed);
        }

        if (canBreakBlocks && EventHooks.canEntityGrief(mob.level(), mob)) {
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
            if (!chargeSwipeDone) {
                mob.doHurtTarget(target);
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
                && EventHooks.onEntityDestroyBlock(entity, pos, state);
    }
}
