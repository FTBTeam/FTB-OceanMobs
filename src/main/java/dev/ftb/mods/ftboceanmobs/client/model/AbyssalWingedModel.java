package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.AbyssalWinged;
import net.minecraft.resources.Identifier;

public class AbyssalWingedModel extends DefaultedEntityGeoModel<AbyssalWinged> {
    private static final Identifier ID = FTBOceanMobs.id("abyssal_winged");

    public AbyssalWingedModel() {
        super(ID);
    }
}
