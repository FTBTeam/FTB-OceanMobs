package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getGenerator().addProvider(
                event.includeServer(),
                (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output -> new DatapackBuiltinEntriesProvider(
                        output,
                        event.getLookupProvider(),
                        new RegistrySetBuilder().add(Registries.ENCHANTMENT, ModEnchantmentProvider::bootstrap),
                        Set.of(FTBOceanMobs.MODID)
                )
        ).getRegistryProvider();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(generator, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(generator.getPackOutput(), existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLangProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new ModSoundProvider(generator.getPackOutput(), existingFileHelper));

        generator.addProvider(event.includeServer(), new ModTagsProvider.EntityType(generator.getPackOutput(), lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModTagsProvider.Block(generator.getPackOutput(), lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModTagsProvider.Enchantment(generator.getPackOutput(), lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(generator.getPackOutput(), lookupProvider));
    }

    public static List<DataProvider> makeProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries, ExistingFileHelper efh) {
        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.ENCHANTMENT, ModEnchantmentProvider::bootstrap);
        return List.of(
                new DatapackBuiltinEntriesProvider(output, vanillaRegistries, builder, Set.of(FTBOceanMobs.MODID))
        );
    }
}
