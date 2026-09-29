package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.ShadowBeast;
import net.minecraft.resources.Identifier;

public class ShadowBeastModel extends DefaultedEntityGeoModel<ShadowBeast> {
    private static final Identifier ID = FTBOceanMobs.id("shadowbeast");

    public ShadowBeastModel() {
        super(ID);
    }
}
