package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FTBOceanMobs.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModBlocks.ABYSSAL_WATER.get(), models().getExistingFile(ResourceLocation.withDefaultNamespace("block/water")));

        ModelFile geyser = models().cubeTop("energy_geyser",
                FTBOceanMobs.id("block/energy_geyser_side"), FTBOceanMobs.id("block/energy_geyser_top"));
        simpleBlockWithItem(ModBlocks.ENERGY_GEYSER.get(),geyser);

        simpleBlockWithItem(ModBlocks.SLUDGE_BLOCK.get(), models().withExistingParent("block/sludge_block", "block/block")
                .texture("particle", FTBOceanMobs.id("block/sludge_block"))
                .texture("texture", FTBOceanMobs.id("block/sludge_block"))
                .renderType("translucent")
                .element().from(3f, 3f, 3f).to(13f,13f, 13f)
                .allFaces((dir, builder) -> builder.uvs(3f, 3f, 13f, 13f).texture("#texture")).end()
                .element().from(0f, 0f, 0f).to(16f, 16f, 16f)
                .allFaces((dir, builder) -> builder.uvs(0f, 0f, 16f, 16f).texture("#texture").cullface(dir)).end());
    }
}
