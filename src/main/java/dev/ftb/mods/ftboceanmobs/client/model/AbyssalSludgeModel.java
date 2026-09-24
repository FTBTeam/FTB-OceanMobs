package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalSludge;
import net.minecraft.resources.Identifier;

public class AbyssalSludgeModel extends DefaultedEntityGeoModel<AbyssalSludge> {
    private static final Identifier ID = FTBOceanMobs.id("abyssal_sludge");

    public AbyssalSludgeModel() {
        super(ID);
    }
}
