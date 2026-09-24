package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.RiftDemon;
import net.minecraft.resources.Identifier;

public class RiftDemonModel extends DefaultedEntityGeoModel<RiftDemon> {
    private static final Identifier ID = FTBOceanMobs.id("rift_demon");

    public RiftDemonModel() {
        super(ID);
    }
}
