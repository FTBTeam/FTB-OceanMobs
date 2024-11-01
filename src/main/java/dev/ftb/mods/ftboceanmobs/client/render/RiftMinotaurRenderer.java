package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ftb.mods.ftboceanmobs.client.model.RiftMinotaurModel;
import dev.ftb.mods.ftboceanmobs.entity.RiftMinotaur;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class RiftMinotaurRenderer extends GeoEntityRenderer<RiftMinotaur> {
    private static final String LEFT_HAND = "bone8";
    private static final String RIGHT_HAND = "bone13";

    private ItemStack mainHandItem;
    private ItemStack offhandItem;

    public RiftMinotaurRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftMinotaurModel());

        addRenderLayer(new MinotaurHeldLayer());
    }

    public static RiftMinotaurRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (RiftMinotaurRenderer) new RiftMinotaurRenderer(renderManager).withScale(scale);
    }

    @Override
    public void preRender(PoseStack poseStack, RiftMinotaur animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        this.mainHandItem = animatable.getMainHandItem();
        this.offhandItem = animatable.getOffhandItem();
    }

    private class MinotaurHeldLayer extends BlockAndItemGeoLayer<RiftMinotaur> {
        public MinotaurHeldLayer() {
            super(RiftMinotaurRenderer.this);
        }

        @Override
        protected ItemStack getStackForBone(GeoBone bone, RiftMinotaur animatable) {
            return switch (bone.getName()) {
                case LEFT_HAND -> animatable.isLeftHanded() ? mainHandItem : offhandItem;
                case RIGHT_HAND -> animatable.isLeftHanded() ? offhandItem : mainHandItem;
                default -> null;
            };
        }

        @Override
        protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, RiftMinotaur animatable) {
            return switch (bone.getName()) {
                case LEFT_HAND, RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                default -> ItemDisplayContext.NONE;
            };
        }

        // Do some quick render modifications depending on what the item is
        @Override
        protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, RiftMinotaur animatable,
                                          MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
            if (stack == mainHandItem) {
                // the axe
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.translate(0.2, 0.15, -0.32);
                poseStack.scale(2f,2f, 2f);
            } else if (stack == offhandItem) {
                // a block about to be thrown
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.translate(0, 0.125, -0.5);
                poseStack.scale(2f,2f, 2f);
            }

            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        }
    }
}
