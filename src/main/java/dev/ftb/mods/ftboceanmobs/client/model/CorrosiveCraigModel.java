package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import net.minecraft.resources.Identifier;

public class CorrosiveCraigModel extends DefaultedEntityGeoModel<CorrosiveCraig> {
    private static final Identifier ID = FTBOceanMobs.id("corrosive_craig");

    public CorrosiveCraigModel() {
        super(ID);
    }
}
