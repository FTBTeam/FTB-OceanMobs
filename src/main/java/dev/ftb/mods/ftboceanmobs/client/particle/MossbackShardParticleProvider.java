package dev.ftb.mods.ftboceanmobs.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class MossbackShardParticleProvider implements ParticleProvider<SimpleParticleType> {
    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return new MossbackShardParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    public static class MossbackShardParticle extends BreakingItemParticle {
        protected MossbackShardParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(Items.AMETHYST_SHARD));
            setLifetime(20);
            gravity = 0.1F;
        }
    }
}
