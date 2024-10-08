package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public class SludgelingRenderer extends SlimeRenderer {
    private static final ResourceLocation SLUDGELING_TEXTURE = FTBOceanMobs.id("textures/entity/sludgeling.png");

    public SludgelingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return SLUDGELING_TEXTURE;
    }
}
