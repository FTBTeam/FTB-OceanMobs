package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.RiftMinotaur;
import net.minecraft.resources.Identifier;

public class RiftMinotaurModel extends DefaultedEntityGeoModel<RiftMinotaur> {
    private static final Identifier ID = FTBOceanMobs.id("rift_minotaur");

    public RiftMinotaurModel() {
        super(ID);
    }
}
