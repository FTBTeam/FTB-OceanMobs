package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    protected ModSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, FTBOceanMobs.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.MINOTAUR_IDLE, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("minotaur_idle")))
                .subtitle("ftboceanmobs.subtitle.minotaur_grunt"));
    }
}
