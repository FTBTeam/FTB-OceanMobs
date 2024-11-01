package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class CorrosiveCraigModel extends DefaultedEntityGeoModel<CorrosiveCraig> {
    private static final ResourceLocation ID = FTBOceanMobs.id("corrosive_craig");

    public CorrosiveCraigModel() {
        super(ID, true);
    }
}
