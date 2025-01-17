package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.CorrosiveCraigModel;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class CorrosiveCraigRenderer extends GeoEntityRenderer<CorrosiveCraig> {
    private static final String RIGHT_HAND = "bone6";

    public CorrosiveCraigRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CorrosiveCraigModel());

        addRenderLayer(new FlameLayer());
    }

    public static CorrosiveCraigRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (CorrosiveCraigRenderer) new CorrosiveCraigRenderer(renderManager).withScale(scale);
    }

    private class FlameLayer extends BlockAndItemGeoLayer<CorrosiveCraig> {
        public FlameLayer() {
            super(CorrosiveCraigRenderer.this);
        }

        @Override
        protected @Nullable BlockState getBlockForBone(GeoBone bone, CorrosiveCraig animatable) {
            return animatable.swinging && animatable.getEntityData().get(CorrosiveCraig.FIRE_FIST) && bone.getName().equals(RIGHT_HAND) ?
                    Blocks.FIRE.defaultBlockState() : null;
        }

        @Override
        protected void renderBlockForBone(PoseStack poseStack, GeoBone bone, BlockState state, CorrosiveCraig animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
            poseStack.scale(2.5f, 2.5f, 2.5f);
            super.renderBlockForBone(poseStack, bone, state, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        }
    }
}
