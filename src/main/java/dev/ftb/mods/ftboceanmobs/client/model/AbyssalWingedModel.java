package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalWinged;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AbyssalWingedModel extends DefaultedEntityGeoModel<AbyssalWinged> {
    private static final ResourceLocation ABYSSAL_WINGED = FTBOceanMobs.id("abyssal_winged");

    public AbyssalWingedModel() {
        super(ABYSSAL_WINGED, true);
    }
}
