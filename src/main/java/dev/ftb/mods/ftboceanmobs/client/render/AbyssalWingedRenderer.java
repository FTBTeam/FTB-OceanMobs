package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.ftb.mods.ftboceanmobs.client.model.AbyssalWingedModel;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalWinged;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AbyssalWingedRenderer extends HeadTurningGeoRenderer<AbyssalWinged> {
    public AbyssalWingedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalWingedModel());

        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static AbyssalWingedRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (AbyssalWingedRenderer) new AbyssalWingedRenderer(renderManager).withScale(scale);
    }
}
