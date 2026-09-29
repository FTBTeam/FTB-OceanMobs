/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftboceanmobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftboceanmobs.entity.TumblingBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class TumblingBlockRenderer extends EntityRenderer<TumblingBlockEntity, TumblingBlockRenderer.TumblingBlockRenderState> {
    public TumblingBlockRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public TumblingBlockRenderState createRenderState() {
        return new TumblingBlockRenderState();
    }

    @Override
    public void extractRenderState(TumblingBlockEntity entity, TumblingBlockRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        ItemStack stack = entity.getStack();
        BlockState blockState = !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem ?
                blockItem.getBlock().defaultBlockState() :
                Blocks.AIR.defaultBlockState();
        BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        state.movingBlockRenderState.randomSeedPos = entity.getOrigin();
        state.movingBlockRenderState.blockPos = blockpos;
        state.movingBlockRenderState.blockState = blockState;
        if (entity.level() instanceof ClientLevel clientLevel) {
            state.movingBlockRenderState.biome = clientLevel.getBiome(blockpos);
            state.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
            state.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
        }
        state.sameAsBlockAtPos = blockState == entity.level().getBlockState(entity.blockPosition());
        state.tumbleVec = entity.tumbleVec;
        state.tumbleAngle = (entity.tickCount + partialTicks) * 18;
    }

    @Override
    public void submit(TumblingBlockRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BlockState blockState = state.movingBlockRenderState.blockState;
        if (blockState.getRenderShape() == RenderShape.MODEL && !state.sameAsBlockAtPos) {
            matrixStackIn.pushPose();
            if (state.tumbleVec != null) {
                // spin the block on the x & z axes
                matrixStackIn.translate(0, 0.5, 0);
                matrixStackIn.mulPose(Axis.of(state.tumbleVec).rotationDegrees(state.tumbleAngle));
                matrixStackIn.translate(-0.5, -0.5, -0.5);
            }
            submitNodeCollector.submitMovingBlock(matrixStackIn, state.movingBlockRenderState);
            matrixStackIn.popPose();
        }
    }

    public static class TumblingBlockRenderState extends FallingBlockRenderState {
        @Nullable
        public Vector3f tumbleVec;
        public float tumbleAngle;
        public boolean sameAsBlockAtPos;
    }
}
