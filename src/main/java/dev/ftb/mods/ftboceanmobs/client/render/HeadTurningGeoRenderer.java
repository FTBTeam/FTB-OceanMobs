package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import dev.ftb.mods.ftboceanmobs.entity.AnimatedHeadTracking;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class HeadTurningGeoRenderer<T extends Mob & GeoEntity> extends GeoEntityRenderer<T, LivingEntityRenderState> {
    private static final String HEAD_BONE = "head";
    private static final DataTicket<Float> HEAD_TRACKING_WEIGHT = DataTicket.create("ftboceanmobs:head_tracking_weight", Float.class);

    public HeadTurningGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void addRenderData(T entity, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(entity, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HEAD_TRACKING_WEIGHT, entity instanceof AnimatedHeadTracking tracking ? tracking.getHeadTrackingWeight() : 1f);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        float weight = renderPassInfo.getGeckolibData(HEAD_TRACKING_WEIGHT);
        float pitch = Mth.clamp(renderPassInfo.getGeckolibData(DataTickets.ENTITY_PITCH), -45f, 45f) * Mth.DEG_TO_RAD * weight;
        float yaw = Mth.clamp(renderPassInfo.getGeckolibData(DataTickets.ENTITY_YAW), -60f, 60f) * Mth.DEG_TO_RAD * weight;
        snapshots.ifPresent(HEAD_BONE, head -> {
            head.setRotX(head.getRotX() - pitch);
            head.setRotY(head.getRotY() - yaw);
        });
    }
}
