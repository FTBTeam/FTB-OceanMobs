package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation TEMPLATE_SPAWN_EGG = ResourceLocation.parse("item/template_spawn_egg");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), FTBOceanMobs.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "FTB Ocean Mobs Item Models";
    }

    @Override
    protected void registerModels() {
        ModItems.getSpawnEggs().forEach(egg -> {
            withExistingParent(egg.getId().getPath(), TEMPLATE_SPAWN_EGG);
        });
    }
}
