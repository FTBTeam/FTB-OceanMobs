package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.CorrosiveCraigModel;
import dev.ftb.mods.ftboceanmobs.entity.CorrosiveCraig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CorrosiveCraigRenderer extends HeadTurningGeoRenderer<CorrosiveCraig> {
    private static final String RIGHT_HAND = "bone6";

    public CorrosiveCraigRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CorrosiveCraigModel());

        withRenderLayer(new FlameLayer(renderManager, this));
    }

    @Override
    public float getMotionAnimThreshold(CorrosiveCraig animatable) {
        // this mob is slow, needs a lower threshold than normal
        return 0.005f;
    }

    public static CorrosiveCraigRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (CorrosiveCraigRenderer) new CorrosiveCraigRenderer(renderManager).withScale(scale);
    }

    private static class FlameLayer extends BlockAndItemGeoLayer<CorrosiveCraig, Void, LivingEntityRenderState> {
        public FlameLayer(EntityRendererProvider.Context context, GeoRenderer<CorrosiveCraig, Void, LivingEntityRenderState> renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(CorrosiveCraig animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            return animatable.swinging && animatable.getEntityData().get(CorrosiveCraig.FIRE_FIST) ?
                    List.of(RenderData.block(RIGHT_HAND, RenderUtil.createRenderStateForBlock(Blocks.FIRE.defaultBlockState(), blockModelResolver))) :
                    List.of();
        }

        @Override
        public void addRenderData(CorrosiveCraig animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!contents.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, contents);
            }
        }

        @Override
        protected void submitBlockRender(PoseStack poseStack, GeoBone bone, BlockModelRenderState blockState, LivingEntityRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
            poseStack.scale(2.5f, 2.5f, 2.5f);
            super.submitBlockRender(poseStack, bone, blockState, renderState, renderTasks, packedLight);
        }
    }
}
