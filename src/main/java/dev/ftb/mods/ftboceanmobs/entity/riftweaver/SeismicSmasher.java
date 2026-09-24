package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.integration.ftbchunks.FTBChunksIntegration;
import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class SeismicSmasher {
    private static final int MAX_WORKERS = 15;

    private final Level level;
    private final RiftWeaverBoss boss;
    private final Predicate<BlockPos> validator;
    private final BlockPos origin;
    private final int maxRadiusSq;
    private final List<SmashWorker> workers = new ArrayList<>();

    public SeismicSmasher(RiftWeaverBoss boss, BlockPos origin, int maxRadius, int nInitialWorkers, Predicate<BlockPos> validator) {
        this.boss = boss;
        this.level = boss.level();
        this.validator = validator;
        this.origin = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, origin).below();
        this.maxRadiusSq = maxRadius * maxRadius;

        float incr = Mth.TWO_PI / nInitialWorkers;
        float rot = incr * level.getRandom().nextFloat();
        for (int i = 0; i < nInitialWorkers; i++, rot += incr) {
            float fudge = (level.getRandom().nextFloat() * 0.6f - 0.3f);
            workers.add(new SmashWorker(Vec3.atCenterOf(origin), new Vec3(1, 0, 0).yRot(rot + fudge)));
        }
    }

    public boolean tick() {
        if (!(level instanceof ServerLevel serverLevel) || !EventHooks.canEntityGrief(serverLevel, boss)) {
            return false;
        }
        List<SmashWorker> newWorkers = new ArrayList<>();

        for (Iterator<SmashWorker> iterator = workers.iterator(); iterator.hasNext(); ) {
            SmashWorker worker = iterator.next();
            if (!worker.tick()) {
                iterator.remove();
            } else {
                if (workers.size() < MAX_WORKERS && level.getRandom().nextInt(8) == 0) {
                    newWorkers.add(worker.createBranch());
                }
            }
        }

        workers.addAll(newWorkers);

        return !workers.isEmpty();
    }

    private boolean canModifyBlock(BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos) || !validator.test(pos)
                || !FTBChunksIntegration.canMobGriefBlocks(level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.getDestroySpeed(level, pos) >= 0f
                && level.getBlockEntity(pos) == null
                && state.getBlock().canEntityDestroy(state, level, pos, boss)
                && EventHooks.onEntityDestroyBlock(boss, pos, state);
    }

    class SmashWorker {
        private Vec3 pos;
        private final Vec3 direction;

        private SmashWorker(Vec3 pos, Vec3 direction) {
            this.pos = pos;
            this.direction = direction;
        }

        boolean tick() {
            pos = pos.add(direction);
            BlockPos blockPos = BlockPos.containing(pos);
            if (origin.distToCenterSqr(pos) <= maxRadiusSq && validator.test(blockPos)) {
                BlockPos workPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).below();
                BlockState state = level.getBlockState(workPos);
                Registry<Block> blockReg = level.registryAccess().lookupOrThrow(Registries.BLOCK);
                if (state.is(FTBOceanMobsTags.Blocks.SEISMIC_SMASHABLE)) {
                    blockReg.getRandomElementOf(FTBOceanMobsTags.Blocks.SEISMIC_CRACKED, level.getRandom())
                            .ifPresent(h -> {
                                BlockState newState = level.getRandom().nextInt(20) == 0 ?
                                        ModBlocks.ENERGY_GEYSER.get().defaultBlockState() :
                                        h.value().defaultBlockState();
                                BlockPos targetPos = level.getRandom().nextInt(4) == 0 ? workPos.above() : workPos;
                                if (canModifyBlock(targetPos)) {
                                    level.setBlock(targetPos, newState, Block.UPDATE_ALL);
                                }
                            });
                } else if (state.is(FTBOceanMobsTags.Blocks.SEISMIC_CRACKED)) {
                    blockReg.getRandomElementOf(FTBOceanMobsTags.Blocks.SEISMIC_SMASHED, level.getRandom())
                            .ifPresent(h -> {
                                if (canModifyBlock(workPos) && canModifyBlock(workPos.below())) {
                                    level.setBlock(workPos, h.value().defaultBlockState(), Block.UPDATE_ALL);
                                    level.removeBlock(workPos.below(), false);
                                }
                            });
                }
                if (level.getRandom().nextInt(5) == 0) {
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, workPos.above(), Block.getId(state));
                }
                return true;
            }
            return false;
        }

        public SmashWorker createBranch() {
            float yaw = level.getRandom().nextFloat() * 0.5f + 0.35f;
            Vec3 newDir = direction.yRot(level.getRandom().nextBoolean() ? yaw : -yaw);
            return new SmashWorker(pos.add(newDir), newDir);
        }
    }
}
