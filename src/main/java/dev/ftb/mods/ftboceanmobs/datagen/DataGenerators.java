package dev.ftb.mods.ftboceanmobs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(generator, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLangProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new ModSoundProvider(generator.getPackOutput(), existingFileHelper));
    }
}
