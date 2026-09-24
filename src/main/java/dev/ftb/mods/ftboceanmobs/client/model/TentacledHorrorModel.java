package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.resources.Identifier;

public class TentacledHorrorModel extends DefaultedEntityGeoModel<TentacledHorror> {
    private static final Identifier ID = FTBOceanMobs.id("tentacled_horror");

    public TentacledHorrorModel() {
        super(ID);
    }
}
