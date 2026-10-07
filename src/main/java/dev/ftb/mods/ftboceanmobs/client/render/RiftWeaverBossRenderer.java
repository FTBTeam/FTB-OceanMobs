package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftboceanmobs.client.model.RiftWeaverBossModel;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftWeaverBossRenderer extends HeadTurningGeoRenderer<RiftWeaverBoss> {
    public RiftWeaverBossRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftWeaverBossModel());

        withRenderLayer(new FlameLayer(renderManager, this));
    }

    public static RiftWeaverBossRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftWeaverBossRenderer) new RiftWeaverBossRenderer(renderManager).withScale(scale);
    }

    private static class FlameLayer extends BlockAndItemGeoLayer<RiftWeaverBoss, Void, LivingEntityRenderState> {
        private static final String RIGHT_HAND = "bone2";
        private static final String LEFT_HAND = "bone11";

        public FlameLayer(EntityRendererProvider.Context context, GeoRenderer<RiftWeaverBoss, Void, LivingEntityRenderState> renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(RiftWeaverBoss animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            if (!animatable.isFrenzied()) {
                return List.of();
            }
            BlockModelRenderState soulFire = RenderUtil.createRenderStateForBlock(Blocks.SOUL_FIRE.defaultBlockState(), blockModelResolver);
            return List.of(RenderData.block(RIGHT_HAND, soulFire), RenderData.block(LEFT_HAND, soulFire));
        }

        @Override
        public void addRenderData(RiftWeaverBoss animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!contents.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, contents);
            }
        }

        @Override
        protected void submitBlockRender(PoseStack poseStack, GeoBone bone, BlockModelRenderState blockState, LivingEntityRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
            poseStack.scale(1.3f, 1.6f, 1.3f);
            super.submitBlockRender(poseStack, bone, blockState, renderState, renderTasks, packedLight);
        }
    }
}
