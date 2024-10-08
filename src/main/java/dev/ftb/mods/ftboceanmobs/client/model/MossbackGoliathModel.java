package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.MossbackGoliath;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class MossbackGoliathModel extends DefaultedEntityGeoModel<MossbackGoliath> {
    private static final ResourceLocation MOSSBACK_GOLIATH = FTBOceanMobs.id("mossback_goliath");

    public MossbackGoliathModel() {
        super(MOSSBACK_GOLIATH, true);
    }
}
