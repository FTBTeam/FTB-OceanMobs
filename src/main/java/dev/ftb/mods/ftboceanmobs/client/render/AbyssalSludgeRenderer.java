package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.AbyssalSludgeModel;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalSludge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbyssalSludgeRenderer extends GeoEntityRenderer<AbyssalSludge> {
    public AbyssalSludgeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalSludgeModel());
    }

    public static AbyssalSludgeRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (AbyssalSludgeRenderer) new AbyssalSludgeRenderer(renderManager).withScale(scale);
    }
}
