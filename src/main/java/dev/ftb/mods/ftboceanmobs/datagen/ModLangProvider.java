package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
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
    }

    private void addEntityAndEgg(EntityType<? extends Entity> entityType, Item spawnEgg, String translation) {
        add(entityType, translation);
        add(spawnEgg, translation + " Spawn Egg");
    }
}
