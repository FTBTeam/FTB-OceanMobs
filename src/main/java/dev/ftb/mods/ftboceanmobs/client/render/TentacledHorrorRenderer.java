package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.TentacledHorrorModel;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TentacledHorrorRenderer extends GeoEntityRenderer<TentacledHorror> {
    public TentacledHorrorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TentacledHorrorModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static TentacledHorrorRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (TentacledHorrorRenderer) new TentacledHorrorRenderer(renderManager).withScale(scale);
    }
}
