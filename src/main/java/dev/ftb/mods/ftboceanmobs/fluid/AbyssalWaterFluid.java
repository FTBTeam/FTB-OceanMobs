package dev.ftb.mods.ftboceanmobs.fluid;

import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import dev.ftb.mods.ftboceanmobs.registry.ModFluids;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class AbyssalWaterFluid {
    public static final FluidRenderProps RENDER_PROPS = new FluidRenderProps
            ("minecraft:block/water_still", "minecraft:block/water_flow", 0x80592693);

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.ABYSSAL_WATER_TYPE, ModFluids.ABYSSAL_WATER, ModFluids.ABYSSAL_WATER_FLOWING
        ).block(ModBlocks.ABYSSAL_WATER).bucket(ModItems.ABYSSAL_WATER_BUCKET).tickRate(5);
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(props());
        }

        @Override
        public boolean canHydrate(FluidState state, BlockGetter getter, BlockPos pos, BlockState source, BlockPos sourcePos) {
            return source.getBlock() == Blocks.SPONGE || super.canHydrate(state, getter, pos, source, sourcePos);
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }

        @Override
        public boolean canHydrate(FluidState state, BlockGetter getter, BlockPos pos, BlockState source, BlockPos sourcePos) {
            return source.getBlock() == Blocks.SPONGE || super.canHydrate(state, getter, pos, source, sourcePos);
        }

        @Override
        protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            if (!state.getValue(FALLING)) {
                if (random.nextInt(64) == 0) {
                    level.playLocalSound(
                            (double)pos.getX() + 0.5,
                            (double)pos.getY() + 0.5,
                            (double)pos.getZ() + 0.5,
                            SoundEvents.WATER_AMBIENT,
                            SoundSource.BLOCKS,
                            random.nextFloat() * 0.25F + 0.75F,
                            random.nextFloat() + 0.5F,
                            false
                    );
                }
            }
        }
    }
}
