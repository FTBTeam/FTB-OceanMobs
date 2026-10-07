package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.ftb.mods.ftboceanmobs.client.model.ShadowBeastModel;
import dev.ftb.mods.ftboceanmobs.entity.ShadowBeast;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ShadowBeastRenderer extends HeadTurningGeoRenderer<ShadowBeast> {
    public ShadowBeastRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShadowBeastModel());

        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static ShadowBeastRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (ShadowBeastRenderer) new ShadowBeastRenderer(renderManager).withScale(scale);
    }
}
