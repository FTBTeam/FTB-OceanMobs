package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.AbyssalSludgeModel;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalSludge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AbyssalSludgeRenderer extends HeadTurningGeoRenderer<AbyssalSludge> {
    public AbyssalSludgeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalSludgeModel());
    }

    public static AbyssalSludgeRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (AbyssalSludgeRenderer) new AbyssalSludgeRenderer(renderManager).withScale(scale);
    }
}
