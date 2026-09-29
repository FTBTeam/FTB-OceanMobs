package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.MossbackGoliath;
import net.minecraft.resources.Identifier;

public class MossbackGoliathModel extends DefaultedEntityGeoModel<MossbackGoliath> {
    private static final Identifier ID = FTBOceanMobs.id("mossback_goliath");

    public MossbackGoliathModel() {
        super(ID);
    }
}
