package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FTBOceanMobs.MODID, value = Dist.CLIENT)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder().add(Registries.ENCHANTMENT, ModEnchantmentProvider::bootstrap),
                Set.of(FTBOceanMobs.MODID)
        );

        PackOutput packOutput = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.addProvider(new ModModelProvider(packOutput));
        event.addProvider(new ModLangProvider(packOutput));
        event.addProvider(new ModSoundProvider(packOutput));

        event.addProvider(new ModTagsProvider.EntityType(packOutput, lookupProvider));
        event.addProvider(new ModTagsProvider.Block(packOutput, lookupProvider));
        event.addProvider(new ModTagsProvider.Enchantment(packOutput, lookupProvider));
        event.addProvider(new ModLootTableProvider(packOutput, lookupProvider));
    }
}
