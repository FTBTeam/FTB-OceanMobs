package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, FTBOceanMobs.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_SLUDGE_AMBIENT = register("abyssal_sludge_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_WINGED_AMBIENT = register("abyssal_winged_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_WINGED_DEATH = register("abyssal_winged_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORROSIVE_CRAIG_ATTACK = register("corrosive_craig_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORROSIVE_CRAIG_DEATH = register("corrosive_craig_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINOTAUR_AMBIENT = register("minotaur_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOSSBACK_GOLIATH_DEATH = register("mossback_goliath_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_DEMON_ATTACK = register("rift_demon_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_DEMON_DEATH = register("rift_demon_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_DEMON_LIGHTNING = register("rift_demon_lightning");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_DEMON_SHIELD = register("rift_demon_shield");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFTLING_OBSERVER_AMBIENT = register("riftling_observer_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFTLING_OBSERVER_DEATH = register("riftling_observer_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_WEAVER_AMBIENT = register("rift_weaver_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_WEAVER_ATTACK = register("rift_weaver_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_WEAVER_DEATH = registerFixed("rift_weaver_death", 64f);
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_WEAVER_HURT = register("rift_weaver_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_WEAVER_SUMMON = registerFixed("rift_weaver_summon", 64f);
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOWBEAST_AMBIENT = register("shadowbeast_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOWBEAST_ATTACK = register("shadowbeast_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOWBEAST_DEATH = register("shadowbeast_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOWBEAST_ROAR = register("shadowbeast_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_AMBIENT = register("tentacled_horror_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_DEATH = register("tentacled_horror_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_HURT = register("tentacled_horror_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_SQUEEZE = register("tentacled_horror_squeeze");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_STEP = register("tentacled_horror_step");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLED_HORROR_THROW = register("tentacled_horror_throw");


    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FTBOceanMobs.id(name)));
    }

    private static DeferredHolder<SoundEvent,SoundEvent> registerFixed(String name, float range) {
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(FTBOceanMobs.id(name), range));
    }
}
