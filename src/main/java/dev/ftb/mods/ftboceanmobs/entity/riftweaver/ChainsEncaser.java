package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import dev.ftb.mods.ftboceanmobs.entity.TumblingBlockEntity;
import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class ChainsEncaser {
    private final LivingEntity target;
    private final int totalTickTime;
    private final List<BlockPos> positions;

    private int tickCount;
    private int posIndex;

    public ChainsEncaser(LivingEntity target, int totalTickTime) {
        this.target = target;
        this.totalTickTime = totalTickTime;

        tickCount = posIndex = 0;
        positions = BlockPos.betweenClosedStream(target.getBoundingBox().inflate(1))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingInt(Vec3i::getY))
                .toList();
    }

    public boolean tick(RiftWeaverBoss boss) {
        if (!target.isAlive()) {
            return false;
        }

        float progress = Mth.clamp(tickCount / (float) totalTickTime, 0f, 1f);
        int tgtIndex = (int) (positions.size() * progress);

        Vec3 viewVec = boss.getViewVector(1f).normalize();
        for (int i = posIndex; i < tgtIndex; i++) {
            float radius = boss.level().random.nextFloat() * RiftWeaverBoss.ARENA_RADIUS;
            float yOff = boss.level().random.nextFloat() * (RiftWeaverBoss.ARENA_HEIGHT - 10) + 10;
            float angle = boss.level().random.nextFloat() * Mth.TWO_PI;
            Vec3 launchPos = Vec3.atCenterOf(boss.getSpawnPos()).add(Mth.cos(angle) * radius, yOff, Mth.sin(angle) * radius);
            Vec3 tgtPos = Vec3.atBottomCenterOf(positions.get(posIndex)).add(0, 1, 0);
            TumblingBlockEntity t = new TumblingBlockEntity(boss.level(), boss, launchPos.x, launchPos.y, launchPos.z, ModBlocks.SLUDGE_BLOCK.toStack())
                    .setCanDropItem(false)
                    .setHitBehaviour(TumblingBlockEntity.HitBehaviour.PLACE_BLOCK_ON_ENTITY);
            t.setDeltaMovement(tgtPos.subtract(launchPos).scale(0.067));
            t.setOnGround(false);
            t.horizontalCollision = false;
            t.verticalCollision = false;
//            target.level().setBlock(positions.get(i), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), Block.UPDATE_ALL);
            launchPos = launchPos.add(viewVec);
            boss.level().addFreshEntity(t);
        }
        posIndex = tgtIndex;

        return ++tickCount < totalTickTime;
    }
}
