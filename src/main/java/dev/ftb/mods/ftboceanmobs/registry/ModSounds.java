package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, FTBOceanMobs.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MINOTAUR_IDLE = register("minotaur_idle");

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FTBOceanMobs.id(name)));
    }
}
