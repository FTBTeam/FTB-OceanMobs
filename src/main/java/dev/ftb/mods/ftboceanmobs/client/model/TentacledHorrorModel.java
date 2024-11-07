package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.ShadowBeast;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TentacledHorrorModel extends DefaultedEntityGeoModel<TentacledHorror> {
    private static final ResourceLocation ID = FTBOceanMobs.id("tentacled_horror");

    public TentacledHorrorModel() {
        super(ID, true);
    }
}
