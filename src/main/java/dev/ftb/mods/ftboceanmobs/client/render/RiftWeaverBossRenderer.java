package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.RiftWeaverBossModel;
import dev.ftb.mods.ftboceanmobs.client.model.RiftWeaverBossNoArmorModel;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class RiftWeaverBossRenderer extends GeoEntityRenderer<RiftWeaverBoss> {
    private final GeoModel<RiftWeaverBoss> modelNoArmor;

    public RiftWeaverBossRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftWeaverBossModel());

        modelNoArmor = new RiftWeaverBossNoArmorModel();

        addRenderLayer(new FlameLayer());
    }

    public static RiftWeaverBossRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftWeaverBossRenderer) new RiftWeaverBossRenderer(renderManager).withScale(scale);
    }

    @Override
    public GeoModel<RiftWeaverBoss> getGeoModel() {
        return getAnimatable().isArmorActive() ? model : modelNoArmor;
    }

    private class FlameLayer extends BlockAndItemGeoLayer<RiftWeaverBoss> {
        private static final String RIGHT_HAND = "bone2";
        private static final String LEFT_HAND = "bone11";

        public FlameLayer() {
            super(RiftWeaverBossRenderer.this);
        }

        @Override
        protected @Nullable BlockState getBlockForBone(GeoBone bone, RiftWeaverBoss animatable) {
            return animatable.isFrenzied() && (bone.getName().equals(RIGHT_HAND) || bone.getName().equals(LEFT_HAND)) ?
                    Blocks.SOUL_FIRE.defaultBlockState() :
                    null;
        }

        @Override
        protected void renderBlockForBone(PoseStack poseStack, GeoBone bone, BlockState state, RiftWeaverBoss animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
            poseStack.scale(1.3f, 1.6f, 1.3f);
            super.renderBlockForBone(poseStack, bone, state, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        }
    }
}
