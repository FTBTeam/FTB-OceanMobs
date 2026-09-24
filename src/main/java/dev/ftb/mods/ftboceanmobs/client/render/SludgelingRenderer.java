package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public class SludgelingRenderer extends SlimeRenderer {
    private static final Identifier SLUDGELING_TEXTURE = FTBOceanMobs.id("textures/entity/sludgeling.png");

    public SludgelingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return SLUDGELING_TEXTURE;
    }
}
