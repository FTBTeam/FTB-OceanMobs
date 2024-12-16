package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.mobeffects.DrowningShadowsEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FTBOceanMobs.MODID);

    public static final DeferredHolder<MobEffect,DrowningShadowsEffect> DROWNING_SHADOWS_EFFECT
            = MOB_EFFECTS.register("drowning_shadows", () -> new DrowningShadowsEffect(MobEffectCategory.HARMFUL, 0x601050));
}
