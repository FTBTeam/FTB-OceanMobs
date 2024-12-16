package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RiftWeaverBossNoArmorModel extends DefaultedEntityGeoModel<RiftWeaverBoss> {
    private static final ResourceLocation ID = FTBOceanMobs.id("rift_weaver_no_armor");

    public RiftWeaverBossNoArmorModel() {
        super(ID, true);
    }
}
