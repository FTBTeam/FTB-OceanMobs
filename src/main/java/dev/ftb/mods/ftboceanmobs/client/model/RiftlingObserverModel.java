package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.RiftlingObserver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RiftlingObserverModel extends DefaultedEntityGeoModel<RiftlingObserver> {
    private static final ResourceLocation ID = FTBOceanMobs.id("riftling_observer");

    public RiftlingObserverModel() {
        super(ID);
    }
}
