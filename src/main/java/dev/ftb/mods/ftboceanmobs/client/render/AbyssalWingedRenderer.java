package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.AbyssalWingedModel;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalWinged;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class AbyssalWingedRenderer extends GeoEntityRenderer<AbyssalWinged> {
    public AbyssalWingedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalWingedModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static AbyssalWingedRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (AbyssalWingedRenderer) new AbyssalWingedRenderer(renderManager).withScale(scale);
    }
}
