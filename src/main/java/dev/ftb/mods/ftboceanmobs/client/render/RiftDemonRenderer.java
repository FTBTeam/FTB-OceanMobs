package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.RiftDemonModel;
import dev.ftb.mods.ftboceanmobs.entity.RiftDemon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftDemonRenderer extends HeadTurningGeoRenderer<RiftDemon> {
    private static final String RIGHT_HAND = "bone8";

    public RiftDemonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftDemonModel());

        withRenderLayer(new FlameLayer(renderManager, this));
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static RiftDemonRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftDemonRenderer) new RiftDemonRenderer(renderManager).withScale(scale);
    }

    private static class FlameLayer extends BlockAndItemGeoLayer<RiftDemon, Void, LivingEntityRenderState> {
        public FlameLayer(EntityRendererProvider.Context context, GeoRenderer<RiftDemon, Void, LivingEntityRenderState> renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(RiftDemon animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            return animatable.swinging ?
                    List.of(RenderData.block(RIGHT_HAND, RenderUtil.createRenderStateForBlock(Blocks.FIRE.defaultBlockState(), blockModelResolver))) :
                    List.of();
        }

        @Override
        public void addRenderData(RiftDemon animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!contents.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, contents);
            }
        }

        @Override
        protected void submitBlockRender(PoseStack poseStack, GeoBone bone, BlockModelRenderState blockState, LivingEntityRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
            poseStack.scale(1.2f, 1.2f, 1.2f);
            super.submitBlockRender(poseStack, bone, blockState, renderState, renderTasks, packedLight);
        }
    }
}
