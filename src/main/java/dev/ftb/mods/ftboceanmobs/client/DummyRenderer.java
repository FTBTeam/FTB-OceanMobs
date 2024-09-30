package dev.ftb.mods.ftboceanmobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class DummyRenderer extends EntityRenderer<Mob> {
    private static final ResourceLocation NONE = ResourceLocation.fromNamespaceAndPath(FTBOceanMobs.MODID, "none");

    public DummyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Mob mob, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(mob, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // just rendering the bounding box
        AABB box = mob.getBoundingBox().move(-mob.getX(), -mob.getY(), -mob.getZ());
        renderFrame(poseStack, bufferSource, box, 1/64f, 0.5f, 1f, 1f, 0.4f, LightTexture.FULL_BRIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(Mob entity) {
        return NONE;
    }

    private static void renderFrame(PoseStack matrixStack, MultiBufferSource buffer, AABB aabb, float fw, float r, float g, float b, float a, int packedLightIn) {
        RenderType type = ModRenderTypes.AABB_FRAME;
        VertexConsumer builder = buffer.getBuffer(type);
        Matrix4f posMat = matrixStack.last().pose();

        float x1 = (float) aabb.minX;
        float y1 = (float) aabb.minY;
        float z1 = (float) aabb.minZ;
        float x2 = (float) aabb.maxX;
        float y2 = (float) aabb.maxY;
        float z2 = (float) aabb.maxZ;

        renderOffsetAABB(posMat, builder, x1 + fw, y1 - fw, z1 - fw, x2 - fw, y1 + fw, z1 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x1 + fw, y2 - fw, z1 - fw, x2 - fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x1 + fw, y1 - fw, z2 - fw, x2 - fw, y1 + fw, z2 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x1 + fw, y2 - fw, z2 - fw, x2 - fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);

        renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z1 + fw, x1 + fw, y1 + fw, z2 - fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x1 - fw, y2 - fw, z1 + fw, x1 + fw, y2 + fw, z2 - fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z1 + fw, x2 + fw, y1 + fw, z2 - fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x2 - fw, y2 - fw, z1 + fw, x2 + fw, y2 + fw, z2 - fw, r, g, b, a, packedLightIn);

        renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z1 - fw, x1 + fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z1 - fw, x2 + fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z2 - fw, x1 + fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z2 - fw, x2 + fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);
    }

    private static void renderOffsetAABB(Matrix4f posMat, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int packedLightIn) {
        builder.addVertex(posMat, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);

        builder.addVertex(posMat, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);

        builder.addVertex(posMat, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);

        builder.addVertex(posMat, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);

        builder.addVertex(posMat, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);

        builder.addVertex(posMat, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(posMat, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
    }
}
