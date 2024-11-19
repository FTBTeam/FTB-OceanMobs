package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.RiftDemonModel;
import dev.ftb.mods.ftboceanmobs.entity.RiftDemon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class RiftDemonRenderer extends GeoEntityRenderer<RiftDemon> {
    private static final String RIGHT_HAND = "bone8";

    public RiftDemonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftDemonModel());

        addRenderLayer(new FlameLayer());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static RiftDemonRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftDemonRenderer) new RiftDemonRenderer(renderManager).withScale(scale);
    }

    private class FlameLayer extends BlockAndItemGeoLayer<RiftDemon> {
        public FlameLayer() {
            super(RiftDemonRenderer.this);
        }

        @Override
        protected @Nullable BlockState getBlockForBone(GeoBone bone, RiftDemon animatable) {
            return animatable.swinging && bone.getName().equals(RIGHT_HAND) ? Blocks.FIRE.defaultBlockState() : null;
        }

        @Override
        protected void renderBlockForBone(PoseStack poseStack, GeoBone bone, BlockState state, RiftDemon animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
            poseStack.scale(1.2f, 1.2f, 1.2f);
            super.renderBlockForBone(poseStack, bone, state, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        }
    }
}
