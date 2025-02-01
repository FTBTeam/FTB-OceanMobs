package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.*;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, FTBOceanMobs.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEntityAndEgg(ModEntityTypes.RIFTLING_OBSERVER.get(), ModItems.RIFTLING_OBSERVER_SPAWN_EGG.get(), "Riftling Observer");
        addEntityAndEgg(ModEntityTypes.ABYSSAL_WINGED.get(), ModItems.ABYSSAL_WINGED_SPAWN_EGG.get(), "Abyssal Winged");
        addEntityAndEgg(ModEntityTypes.CORROSIVE_CRAIG.get(), ModItems.CORROSIVE_CRAIG_SPAWN_EGG.get(), "Corrosive Craig");
        addEntityAndEgg(ModEntityTypes.MOSSBACK_GOLIATH.get(), ModItems.MOSSBACK_GOLIATH_SPAWN_EGG.get(), "Mossback Goliath");
        addEntityAndEgg(ModEntityTypes.ABYSSAL_SLUDGE.get(), ModItems.ABYSSAL_SLUDGE_SPAWN_EGG.get(), "Abyssal Sludge");
        addEntityAndEgg(ModEntityTypes.SHADOW_BEAST.get(), ModItems.SHADOW_BEAST_SPAWN_EGG.get(), "Shadow Beast");
        addEntityAndEgg(ModEntityTypes.RIFT_MINOTAUR.get(), ModItems.RIFT_MINOTAUR_SPAWN_EGG.get(), "Rift Minotaur");
        addEntityAndEgg(ModEntityTypes.TENTACLED_HORROR.get(), ModItems.TENTACLED_HORROR_SPAWN_EGG.get(), "Tentacled Horror");
        addEntityAndEgg(ModEntityTypes.RIFT_DEMON.get(), ModItems.RIFT_DEMON_SPAWN_EGG.get(), "Rift Demon");
        addEntityAndEgg(ModEntityTypes.RIFT_WEAVER.get(), ModItems.RIFT_WEAVER_SPAWN_EGG.get(), "The Rift Weaver");
        addEntityAndEgg(ModEntityTypes.SLUDGELING.get(), ModItems.SLUDGELING_SPAWN_EGG.get(), "Sludgeling");

        addItem(ModItems.SLUDGE_BALL, "Sludge Ball");
        addItem(ModItems.ABYSSAL_WATER_BUCKET, "Abyssal Water Bucket");

        addBlock(ModBlocks.ENERGY_GEYSER, "Energy Geyser");
        addBlock(ModBlocks.ABYSSAL_WATER, "Abyssal Water");
        addBlock(ModBlocks.SLUDGE_BLOCK, "Sludge Block");

        addEffect(ModMobEffects.DROWNING_SHADOWS_EFFECT, "Drowning Shadows");

        add(ModFluids.ABYSSAL_WATER_TYPE.get().getDescriptionId(), "Abyssal Water");

        add("ftboceanmobs.itemGroup.tab", "FTB Ocean Mobs");
        add("enchantment.ftboceanmobs.rift_disruptor", "Rift Disruptor");
        add("ftboceanmobs.subtitles.minotaur_grunts", "Grunting noise");
        add("ftboceanmobs.subtitles.tentacle_squish", "Squishing sounds");
    }

    private void addEntityAndEgg(EntityType<? extends Entity> entityType, Item spawnEgg, String translation) {
        add(entityType, translation);
        add(spawnEgg, translation + " Spawn Egg");
    }
}
