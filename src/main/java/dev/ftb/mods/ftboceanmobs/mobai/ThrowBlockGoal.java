package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.MiscUtil;
import dev.ftb.mods.ftboceanmobs.entity.TumblingBlockEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ThrowBlockGoal extends Goal {

    private final Mob mob;
    private final int totalTime;
    private final int pickupTime;
    private final int launchTime;
    private final float throwChance;

    private LivingEntity target;
    private int tickCounter;

    /**
     * Have a mob throw a block at its target
     *
     * @param mob         the mob
     * @param totalTime   total time in ticks for the animation to run
     * @param pickupTime  time in ticks after which the block appears in the mob's hands
     * @param launchTime  time in ticks after which the block is launched as an entity
     * @param throwChance chance per tick of the mob deciding to throw something
     */
    public ThrowBlockGoal(Mob mob, int totalTime, int pickupTime, int launchTime, float throwChance) {
        this.mob = mob;
        this.totalTime = reducedTickDelay(totalTime);
        this.throwChance = throwChance;
        this.pickupTime = reducedTickDelay(totalTime - pickupTime);
        this.launchTime = reducedTickDelay(totalTime - launchTime);

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();

        return target != null && target.isAlive()
                && mob.level().random.nextFloat() < throwChance
                && (mob.distanceToSqr(target) > 256 || !MiscUtil.canPathfindToTarget(mob, target, 2.25F));
    }

    @Override
    public boolean canContinueToUse() {
        return target.isAlive() && tickCounter > 0;
    }

    @Override
    public void start() {
        tickCounter = totalTime;
        if (mob instanceof IThrowingMob tm) {
            tm.setThrowing(true);
        }
    }

    @Override
    public void stop() {
        if (mob instanceof IThrowingMob tm) {
            tm.setThrowing(false);
        }
    }

    @Override
    public void tick() {
        tickCounter--;

        mob.getLookControl().setLookAt(target);

        if (tickCounter == pickupTime) {
            // mob picks up the block
            BlockState state = mob.getBlockStateOn();
            mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(state.getBlock()));
            mob.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, mob.blockPosition(), Block.getId(state));
            // TODO option to remove the picked-up block from the world?
        } else if (tickCounter == launchTime) {
            ItemStack stack = mob.getItemBySlot(EquipmentSlot.OFFHAND);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                // mob throws the block
                Vec3 pos = mob.getEyePosition().add(0, 0.8, 0);

                TumblingBlockEntity t = new TumblingBlockEntity(mob.level(), mob, pos.x, pos.y, pos.z, stack);
                t.setDeltaMovement(target.position().add(0, 1, 0).subtract(mob.position()).scale(0.065));
                t.setOnGround(false);
                t.horizontalCollision = false;
                t.verticalCollision = false;

                mob.level().addFreshEntity(t);
            }
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

}
