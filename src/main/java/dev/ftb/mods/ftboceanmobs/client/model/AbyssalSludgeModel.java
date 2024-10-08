package dev.ftb.mods.ftboceanmobs.client.model;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalSludge;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AbyssalSludgeModel extends DefaultedEntityGeoModel<AbyssalSludge> {
    private static final ResourceLocation ABYSSAL_SLUDGE = FTBOceanMobs.id("abyssal_sludge");

    public AbyssalSludgeModel() {
        super(ABYSSAL_SLUDGE, true);
    }
}
