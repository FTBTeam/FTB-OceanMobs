package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.ShadowBeast;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ShadowBeastModel extends DefaultedEntityGeoModel<ShadowBeast> {
    private static final ResourceLocation ID = FTBOceanMobs.id("shadowbeast");

    public ShadowBeastModel() {
        super(ID, true);
    }
}
