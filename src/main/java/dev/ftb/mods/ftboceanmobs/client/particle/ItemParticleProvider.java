package dev.ftb.mods.ftboceanmobs.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ItemParticleProvider(ItemStack stack) implements ParticleProvider<SimpleParticleType> {
    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return new ItemParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, stack);
    }

    public static class ItemParticle extends BreakingItemParticle {
        protected ItemParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ItemStack stack) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed, stack);
            setLifetime(30);
            gravity = 0.1F;
        }
    }
}
