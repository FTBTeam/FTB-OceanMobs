package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.RiftlingObserver;
import net.minecraft.resources.Identifier;

public class RiftlingObserverModel extends DefaultedEntityGeoModel<RiftlingObserver> {
    private static final Identifier ID = FTBOceanMobs.id("riftling_observer");

    public RiftlingObserverModel() {
        super(ID);
    }
}
