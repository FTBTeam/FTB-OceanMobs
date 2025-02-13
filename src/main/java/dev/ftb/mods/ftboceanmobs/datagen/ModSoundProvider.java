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
        add(ModSounds.ABYSSAL_SLUDGE_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("abyssal_sludge_ambient")))
                .subtitle("ftboceanmobs.subtitle.abyssal_sludge_ambient"));
        add(ModSounds.ABYSSAL_WINGED_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("abyssal_winged_ambient")))
                .subtitle("ftboceanmobs.subtitle.abyssal_winged_ambient"));
        add(ModSounds.ABYSSAL_WINGED_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("abyssal_winged_death")))
                .subtitle("ftboceanmobs.subtitle.abyssal_winged_death"));
        add(ModSounds.CORROSIVE_CRAIG_ATTACK, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("corrosive_craig_attack")))
                .subtitle("ftboceanmobs.subtitle.corrosive_craig_attack"));
        add(ModSounds.CORROSIVE_CRAIG_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("corrosive_craig_death")))
                .subtitle("ftboceanmobs.subtitle.corrosive_craig_death"));
        add(ModSounds.MINOTAUR_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("minotaur_ambient")))
                .subtitle("ftboceanmobs.subtitle.minotaur_ambient"));
        add(ModSounds.MOSSBACK_GOLIATH_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("mossback_goliath_death")))
                .subtitle("ftboceanmobs.subtitle.mossback_goliath_death"));
        add(ModSounds.RIFT_DEMON_ATTACK, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_demon_attack")))
                .subtitle("ftboceanmobs.subtitle.rift_demon_attack"));
        add(ModSounds.RIFT_DEMON_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_demon_death")))
                .subtitle("ftboceanmobs.subtitle.rift_demon_death"));
        add(ModSounds.RIFT_DEMON_LIGHTNING, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_demon_lightning")))
                .subtitle("ftboceanmobs.subtitle.rift_demon_lightning"));
        add(ModSounds.RIFT_DEMON_SHIELD, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_demon_shield")))
                .subtitle("ftboceanmobs.subtitle.rift_demon_shield"));
        add(ModSounds.RIFTLING_OBSERVER_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("riftling_observer_ambient")))
                .subtitle("ftboceanmobs.subtitle.riftling_observer_ambient"));
        add(ModSounds.RIFTLING_OBSERVER_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("riftling_observer_death")))
                .subtitle("ftboceanmobs.subtitle.riftling_observer_death"));
        add(ModSounds.RIFT_WEAVER_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_weaver_ambient1")).stream())
                .with(sound(FTBOceanMobs.id("rift_weaver_ambient2")).stream())
                .subtitle("ftboceanmobs.subtitle.rift_weaver_ambient"));
        add(ModSounds.RIFT_WEAVER_ATTACK, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_weaver_attack")))
                .subtitle("ftboceanmobs.subtitle.rift_weaver_attack"));
        add(ModSounds.RIFT_WEAVER_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_weaver_death")).stream().attenuationDistance(64))
                .subtitle("ftboceanmobs.subtitle.rift_weaver_death"));
        add(ModSounds.RIFT_WEAVER_HURT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_weaver_hurt")))
                .subtitle("ftboceanmobs.subtitle.rift_weaver_hurt"));
        add(ModSounds.RIFT_WEAVER_SUMMON, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("rift_weaver_summon")).stream().attenuationDistance(64))
                .subtitle("ftboceanmobs.subtitle.rift_weaver_summon"));
        add(ModSounds.SHADOWBEAST_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("shadowbeast_ambient")))
                .subtitle("ftboceanmobs.subtitle.shadowbeast_ambient"));
        add(ModSounds.SHADOWBEAST_ATTACK, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("shadowbeast_attack")))
                .subtitle("ftboceanmobs.subtitle.shadowbeast_attack"));
        add(ModSounds.SHADOWBEAST_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("shadowbeast_death")))
                .subtitle("ftboceanmobs.subtitle.shadowbeast_death"));
        add(ModSounds.SHADOWBEAST_ROAR, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("shadowbeast_attack")).pitch(0.5f).volume(1.25f))
                .subtitle("ftboceanmobs.subtitle.shadowbeast_roar"));
        add(ModSounds.TENTACLED_HORROR_AMBIENT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_ambient")))
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_ambient"));
        add(ModSounds.TENTACLED_HORROR_DEATH, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_death")))
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_death"));
        add(ModSounds.TENTACLED_HORROR_HURT, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_hurt")))
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_hurt"));
        add(ModSounds.TENTACLED_HORROR_SQUEEZE, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_squeeze")).stream())
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_squeeze"));
        add(ModSounds.TENTACLED_HORROR_STEP, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_step")))
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_step"));
        add(ModSounds.TENTACLED_HORROR_THROW, SoundDefinition.definition()
                .with(sound(FTBOceanMobs.id("tentacled_horror_throw")))
                .subtitle("ftboceanmobs.subtitle.tentacled_horror_throw"));

    }
}
