package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.CorrosiveCraigModel;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CorrosiveCraigRenderer extends GeoEntityRenderer<CorrosiveCraig> {
    public CorrosiveCraigRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CorrosiveCraigModel());
    }

    public static CorrosiveCraigRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (CorrosiveCraigRenderer) new CorrosiveCraigRenderer(renderManager).withScale(scale);
    }
}
