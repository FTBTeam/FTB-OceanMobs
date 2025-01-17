package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModFluids;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation TEMPLATE_SPAWN_EGG = ResourceLocation.parse("item/template_spawn_egg");
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), FTBOceanMobs.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "FTB Ocean Mobs Item Models";
    }

    @Override
    protected void registerModels() {
        ModItems.getSpawnEggs().forEach(egg -> withExistingParent(egg.getId().getPath(), TEMPLATE_SPAWN_EGG));

        withExistingParent("sludge_ball", GENERATED).texture("layer0", "item/sludge_ball");

        withExistingParent("abyssal_water_bucket", ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(ModFluids.ABYSSAL_WATER.get());
    }
}
