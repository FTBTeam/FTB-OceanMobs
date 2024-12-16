package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class SeismicSmasher {
    private static final int MAX_WORKERS = 15;

    private final Level level;
    private final Predicate<BlockPos> validator;
    private final BlockPos origin;
    private final int maxRadiusSq;
    private final List<SmashWorker> workers = new ArrayList<>();

    public SeismicSmasher(Level level, BlockPos origin, int maxRadius, int nInitialWorkers, Predicate<BlockPos> validator) {
        this.level = level;
        this.validator = validator;
        this.origin = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, origin).below();
        this.maxRadiusSq = maxRadius * maxRadius;

        float incr = Mth.TWO_PI / nInitialWorkers;
        float rot = incr * level.random.nextFloat();
        for (int i = 0; i < nInitialWorkers; i++, rot += incr) {
            float fudge = (level.random.nextFloat() * 0.6f - 0.3f);
            workers.add(new SmashWorker(Vec3.atCenterOf(origin), new Vec3(1, 0, 0).yRot(rot + fudge)));
        }
    }

    public boolean tick() {
        List<SmashWorker> newWorkers = new ArrayList<>();

        for (Iterator<SmashWorker> iterator = workers.iterator(); iterator.hasNext(); ) {
            SmashWorker worker = iterator.next();
            if (!worker.tick()) {
                iterator.remove();
            } else {
                if (workers.size() < MAX_WORKERS && level.random.nextInt(8) == 0) {
                    newWorkers.add(worker.createBranch());
                }
            }
        }

        workers.addAll(newWorkers);

        return !workers.isEmpty();
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
                Registry<Block> blockReg = level.registryAccess().registryOrThrow(Registries.BLOCK);
                if (state.is(FTBOceanMobsTags.Blocks.SEISMIC_SMASHABLE)) {
                    blockReg.getRandomElementOf(FTBOceanMobsTags.Blocks.SEISMIC_CRACKED, level.random)
                            .ifPresent(h -> {
                                BlockState newState = level.random.nextInt(20) == 0 ?
                                        ModBlocks.ENERGY_GEYSER.get().defaultBlockState() :
                                        h.value().defaultBlockState();
                                level.setBlock(level.random.nextInt(7) == 0 ? workPos.above() : workPos, newState, Block.UPDATE_ALL);
                            });
                } else if (state.is(FTBOceanMobsTags.Blocks.SEISMIC_CRACKED)) {
                    blockReg.getRandomElementOf(FTBOceanMobsTags.Blocks.SEISMIC_SMASHED, level.random)
                            .ifPresent(h -> {
                                level.setBlock(workPos, h.value().defaultBlockState(), Block.UPDATE_ALL);
                                level.removeBlock(workPos.below(), false);
                            });
                }
                if (level.random.nextInt(5) == 0) {
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, workPos.above(), Block.getId(state));
                }
                return true;
            }
            return false;
        }

        public SmashWorker createBranch() {
            float yaw = level.random.nextFloat() * 0.5f + 0.35f;
            Vec3 newDir = direction.yRot(level.random.nextBoolean() ? yaw : -yaw);
            return new SmashWorker(pos.add(newDir), newDir);
        }
    }
}
