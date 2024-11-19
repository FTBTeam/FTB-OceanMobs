package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ftb.mods.ftboceanmobs.client.model.RiftlingObserverModel;
import dev.ftb.mods.ftboceanmobs.entity.RiftlingObserver;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RiftlingObserverRenderer extends GeoEntityRenderer<RiftlingObserver> {
    public RiftlingObserverRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RiftlingObserverModel());

        addRenderLayer(new EyeBeamLayer());
    }

    // this is pretty much a rip-off of the Guardian eye beam, for now at least
    private class EyeBeamLayer extends GeoRenderLayer<RiftlingObserver> {
        private static final ResourceLocation GUARDIAN_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png");
        private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

        public EyeBeamLayer() {
            super(RiftlingObserverRenderer.this);
        }

        @Override
        public void render(PoseStack poseStack, RiftlingObserver entity, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

            LivingEntity livingentity = entity.getSyncedGazeTarget();
            if (livingentity != null) {
                float gazeScale = entity.getAttackAnimationScale(partialTick);
                float gazeWarmupTime = entity.getClientSideAttackTime() + partialTick;
                float f2 = gazeWarmupTime * 0.5F % 1.0F;
                float eyeY = entity.getEyeHeight();
                poseStack.pushPose();
                poseStack.translate(0.0F, eyeY, 0.0F);
                Vec3 vec3 = getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5, partialTick);
                Vec3 vec31 = getPosition(entity, eyeY, partialTick);
                Vec3 vec32 = vec3.subtract(vec31);
                float f4 = (float)(vec32.length() + 1.0);
                vec32 = vec32.normalize();
                float f5 = (float)Math.acos(vec32.y);
                float f6 = (float)Math.atan2(vec32.z, vec32.x);
                poseStack.mulPose(Axis.YP.rotationDegrees(((float) (Math.PI / 2) - f6) * (180.0F / (float)Math.PI)));
                poseStack.mulPose(Axis.XP.rotationDegrees(f5 * (180.0F / (float)Math.PI)));
                float f7 = gazeWarmupTime * 0.05F * -1.5F;
                float gazeScaleSq = gazeScale * gazeScale;
                int red = 58 + (int)(gazeScaleSq * 130f);
                int green = 50 - (int)(gazeScaleSq * 7f);
                int blue = 188 + (int)(gazeScaleSq * 50f);
                float f11 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
                float f12 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
                float f13 = Mth.cos(f7 + (float) (Math.PI / 4)) * 0.282F;
                float f14 = Mth.sin(f7 + (float) (Math.PI / 4)) * 0.282F;
                float f15 = Mth.cos(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
                float f16 = Mth.sin(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
                float f17 = Mth.cos(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
                float f18 = Mth.sin(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
                float f19 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
                float f20 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
                float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
                float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
                float f23 = Mth.cos(f7 + (float) (Math.PI / 2)) * 0.2F;
                float f24 = Mth.sin(f7 + (float) (Math.PI / 2)) * 0.2F;
                float f25 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
                float f26 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
                float f29 = -1.0F + f2;
                float f30 = f4 * 2.5F + f29;
                VertexConsumer vertexconsumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
                PoseStack.Pose posestack$pose = poseStack.last();
                vertex(vertexconsumer, posestack$pose, f19, f4, f20, red, green, blue, 0.4999F, f30);
                vertex(vertexconsumer, posestack$pose, f19, 0.0F, f20, red, green, blue, 0.4999F, f29);
                vertex(vertexconsumer, posestack$pose, f21, 0.0F, f22, red, green, blue, 0.0F, f29);
                vertex(vertexconsumer, posestack$pose, f21, f4, f22, red, green, blue, 0.0F, f30);
                vertex(vertexconsumer, posestack$pose, f23, f4, f24, red, green, blue, 0.4999F, f30);
                vertex(vertexconsumer, posestack$pose, f23, 0.0F, f24, red, green, blue, 0.4999F, f29);
                vertex(vertexconsumer, posestack$pose, f25, 0.0F, f26, red, green, blue, 0.0F, f29);
                vertex(vertexconsumer, posestack$pose, f25, f4, f26, red, green, blue, 0.0F, f30);
                float f31 = 0.0F;
                if (entity.tickCount % 2 == 0) {
                    f31 = 0.5F;
                }

                vertex(vertexconsumer, posestack$pose, f11, f4, f12, red, green, blue, 0.5F, f31 + 0.5F);
                vertex(vertexconsumer, posestack$pose, f13, f4, f14, red, green, blue, 1.0F, f31 + 0.5F);
                vertex(vertexconsumer, posestack$pose, f17, f4, f18, red, green, blue, 1.0F, f31);
                vertex(vertexconsumer, posestack$pose, f15, f4, f16, red, green, blue, 0.5F, f31);
                poseStack.popPose();
            }
        }
    }

    private static Vec3 getPosition(LivingEntity livingEntity, double yOffset, float partialTick) {
        double d0 = Mth.lerp(partialTick, livingEntity.xOld, livingEntity.getX());
        double d1 = Mth.lerp(partialTick, livingEntity.yOld, livingEntity.getY()) + yOffset;
        double d2 = Mth.lerp(partialTick, livingEntity.zOld, livingEntity.getZ());
        return new Vec3(d0, d1, d2);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int red, int green, int blue, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
