package dev.ftb.mods.ftboceanmobs.client.render;

import dev.ftb.mods.ftboceanmobs.client.model.MossbackGoliathModel;
import dev.ftb.mods.ftboceanmobs.entity.MossbackGoliath;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MossbackGoliathRenderer extends HeadTurningGeoRenderer<MossbackGoliath> {
    public MossbackGoliathRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MossbackGoliathModel());
    }

    public static MossbackGoliathRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (MossbackGoliathRenderer) new MossbackGoliathRenderer(renderManager).withScale(scale);
    }
}
