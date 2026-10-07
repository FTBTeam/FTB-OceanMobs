package dev.ftb.mods.ftboceanmobs.client.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class RiftWeaverBossModel extends DefaultedEntityGeoModel<RiftWeaverBoss> {
    private static final Identifier ID = FTBOceanMobs.id("rift_weaver");
    private static final Identifier NO_ARMOR_ID = FTBOceanMobs.id("rift_weaver_no_armor");
    private static final DataTicket<Boolean> ARMOR_ACTIVE = DataTicket.create("ftboceanmobs:armor_active", Boolean.class);

    private final Identifier noArmorModel;
    private final Identifier noArmorTexture;
    private final Identifier noArmorAnimations;

    public RiftWeaverBossModel() {
        super(ID);

        noArmorModel = buildFormattedModelPath(NO_ARMOR_ID);
        noArmorTexture = buildFormattedTexturePath(NO_ARMOR_ID);
        noArmorAnimations = buildFormattedAnimationPath(NO_ARMOR_ID);
    }

    @Override
    public void addAdditionalStateData(RiftWeaverBoss animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(ARMOR_ACTIVE, animatable.isArmorActive());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return isArmorActive(renderState) ? super.getModelResource(renderState) : noArmorModel;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return isArmorActive(renderState) ? super.getTextureResource(renderState) : noArmorTexture;
    }

    @Override
    public Identifier getAnimationResource(RiftWeaverBoss animatable) {
        return animatable.isArmorActive() ? super.getAnimationResource(animatable) : noArmorAnimations;
    }

    private static boolean isArmorActive(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(ARMOR_ACTIVE, false);
    }
}
