package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.RiftMinotaur;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RiftMinotaurModel extends DefaultedEntityGeoModel<RiftMinotaur> {
    private static final ResourceLocation ID = FTBOceanMobs.id("rift_minotaur");

    public RiftMinotaurModel() {
        super(ID, true);
    }
}
