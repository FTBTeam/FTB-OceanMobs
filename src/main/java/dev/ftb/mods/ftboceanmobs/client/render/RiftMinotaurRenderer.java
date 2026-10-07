package dev.ftb.mods.ftboceanmobs.client.render;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftboceanmobs.client.model.RiftMinotaurModel;
import dev.ftb.mods.ftboceanmobs.entity.RiftMinotaur;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftMinotaurRenderer extends HeadTurningGeoRenderer<RiftMinotaur> {
    private static final String LEFT_HAND = "bone8";
    private static final String RIGHT_HAND = "bone13";

    public RiftMinotaurRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftMinotaurModel());

        withRenderLayer(new MinotaurHeldLayer(renderManager, this));
    }

    public static RiftMinotaurRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftMinotaurRenderer) new RiftMinotaurRenderer(renderManager).withScale(scale);
    }

    private static class MinotaurHeldLayer extends BlockAndItemGeoLayer<RiftMinotaur, Void, LivingEntityRenderState> {
        public MinotaurHeldLayer(EntityRendererProvider.Context context, GeoRenderer<RiftMinotaur, Void, LivingEntityRenderState> renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(RiftMinotaur animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            ItemStack mainHandItem = animatable.getMainHandItem();
            ItemStack offhandItem = animatable.getOffhandItem();
            boolean leftHanded = animatable.isLeftHanded();

            return List.of(
                    itemForBone(LEFT_HAND, leftHanded ? mainHandItem : offhandItem, animatable),
                    itemForBone(RIGHT_HAND, leftHanded ? offhandItem : mainHandItem, animatable)
            );
        }

        private RenderData itemForBone(String boneName, ItemStack stack, RiftMinotaur animatable) {
            return RenderData.item(boneName, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    RenderUtil.createRenderStateForItem(stack, itemModelResolver, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable));
        }

        @Override
        public void addRenderData(RiftMinotaur animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            renderState.addGeckolibData(CONTENTS, getRelevantBones(animatable, relatedObject, renderState, partialTick));
            renderState.addGeckolibData(DataTickets.IS_LEFT_HANDED, animatable.isLeftHanded());
        }

        // Do some quick render modifications depending on what the item is
        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackState, ItemDisplayContext displayContext,
                                             LivingEntityRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
            boolean leftHanded = renderState.getOrDefaultGeckolibData(DataTickets.IS_LEFT_HANDED, false);
            boolean isMainHand = bone.name().equals(LEFT_HAND) == leftHanded;
            if (isMainHand) {
                // the axe
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.translate(0.2, 0.15, -0.32);
                poseStack.scale(2f,2f, 2f);
            } else {
                // a block about to be thrown
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.translate(0, 0.125, -0.5);
                poseStack.scale(2f,2f, 2f);
            }

            super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        }
    }
}
