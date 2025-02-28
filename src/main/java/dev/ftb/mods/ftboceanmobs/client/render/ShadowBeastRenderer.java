package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.ShadowBeastModel;
import dev.ftb.mods.ftboceanmobs.entity.ShadowBeast;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ShadowBeastRenderer extends GeoEntityRenderer<ShadowBeast> {
    public ShadowBeastRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShadowBeastModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static ShadowBeastRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (ShadowBeastRenderer) new ShadowBeastRenderer(renderManager).withScale(scale);
    }
}
