package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES
            = DeferredRegister.create(Registries.PARTICLE_TYPE, FTBOceanMobs.MODID);

    public static final Supplier<SimpleParticleType> SLUDGE
            = PARTICLES.register("sludge", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> MOSSBACK_SHARD
            = PARTICLES.register("mossback_shard", () -> new SimpleParticleType(false));
}
