package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import dev.ftb.mods.ftboceanmobs.entity.RiftDemon;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RiftDemonModel extends DefaultedEntityGeoModel<RiftDemon> {
    private static final ResourceLocation ID = FTBOceanMobs.id("rift_demon");

    public RiftDemonModel() {
        super(ID, true);
    }
}
