package dev.ftb.mods.ftboceanmobs.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public class ItemParticleProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
    private final ItemStackTemplate item;

    public ItemParticleProvider(ItemStackTemplate item) {
        this.item = item;
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
        return new ItemParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, getSprite(item, level, random));
    }

    public static class ItemParticle extends BreakingItemParticle {
        protected ItemParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            setLifetime(30);
            gravity = 0.1F;
        }
    }
}
