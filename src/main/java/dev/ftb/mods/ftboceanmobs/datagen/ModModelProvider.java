package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModBlocks;
import dev.ftb.mods.ftboceanmobs.registry.ModFluids;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;

import java.util.Optional;

public class ModModelProvider extends ModelProvider {
    private static final ExtendedModelTemplate SLUDGE_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .element(element -> element.from(3f, 3f, 3f).to(13f, 13f, 13f)
                    .allFaces((dir, face) -> face.uvs(3f, 3f, 13f, 13f).texture(TextureSlot.TEXTURE)))
            .element(element -> element.from(0f, 0f, 0f).to(16f, 16f, 16f)
                    .allFaces((dir, face) -> face.uvs(0f, 0f, 16f, 16f).texture(TextureSlot.TEXTURE).cullface(dir)))
            .build();

    public ModModelProvider(PackOutput output) {
        super(output, FTBOceanMobs.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.ABYSSAL_WATER.get(),
                BlockModelGenerators.plainVariant(Identifier.withDefaultNamespace("block/water"))));

        blockModels.createTrivialBlock(ModBlocks.ENERGY_GEYSER.get(), TexturedModel.CUBE_TOP);

        Identifier sludgeModel = SLUDGE_BLOCK.create(ModBlocks.SLUDGE_BLOCK.get(), TextureMapping.defaultTexture(ModBlocks.SLUDGE_BLOCK.get()), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.SLUDGE_BLOCK.get(),
                BlockModelGenerators.plainVariant(sludgeModel)));

        ModItems.getSpawnEggs().forEach(egg -> itemModels.generateFlatItem(egg.get(), ModelTemplates.FLAT_ITEM));

        itemModels.generateFlatItem(ModItems.SLUDGE_BALL.get(), ModelTemplates.FLAT_ITEM);

        itemModels.itemModelOutput.accept(ModItems.ABYSSAL_WATER_BUCKET.get(), new DynamicFluidContainerModel.Unbaked(
                new DynamicFluidContainerModel.Textures(
                        Optional.empty(),
                        Optional.of(new Material(Identifier.withDefaultNamespace("item/bucket"))),
                        Optional.of(new Material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"))),
                        Optional.empty()
                ),
                ModFluids.ABYSSAL_WATER.get(), false, true, true
        ));
    }
}
