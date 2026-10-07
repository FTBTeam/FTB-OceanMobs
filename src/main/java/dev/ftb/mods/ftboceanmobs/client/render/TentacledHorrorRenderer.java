package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.ftb.mods.ftboceanmobs.client.model.TentacledHorrorModel;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TentacledHorrorRenderer extends HeadTurningGeoRenderer<TentacledHorror> {
    public TentacledHorrorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TentacledHorrorModel());

        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static TentacledHorrorRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (TentacledHorrorRenderer) new TentacledHorrorRenderer(renderManager).withScale(scale);
    }
}
