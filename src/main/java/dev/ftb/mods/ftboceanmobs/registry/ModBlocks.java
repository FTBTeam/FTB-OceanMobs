package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.block.AbyssalWaterBlock;
import dev.ftb.mods.ftboceanmobs.block.EnergyGeyserBlock;
import dev.ftb.mods.ftboceanmobs.block.SludgeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBOceanMobs.MODID);

    public static final DeferredBlock<LiquidBlock> ABYSSAL_WATER = BLOCKS.registerBlock("abyssal_water",
            props -> new AbyssalWaterBlock(ModFluids.ABYSSAL_WATER.get(), props), fluidProps());

    public static final DeferredBlock<EnergyGeyserBlock> ENERGY_GEYSER = BLOCKS.registerBlock("energy_geyser",
            EnergyGeyserBlock::new, BlockBehaviour.Properties.of());

    public static final DeferredBlock<SludgeBlock> SLUDGE_BLOCK = BLOCKS.registerBlock("sludge_block",
            SludgeBlock::new, BlockBehaviour.Properties.of().noOcclusion().friction(0.8f).sound(SoundType.SLIME_BLOCK).mapColor(MapColor.COLOR_PURPLE).strength(0.75f));

    //-----------------------

    private static Block.Properties fluidProps() {
        return Block.Properties.of()
                .mapColor(MapColor.WATER)
                .noCollission()
                .strength(100f)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .replaceable();
    }
}
