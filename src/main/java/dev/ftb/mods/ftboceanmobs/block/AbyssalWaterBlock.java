package dev.ftb.mods.ftboceanmobs.block;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class AbyssalWaterBlock extends LiquidBlock {
    public AbyssalWaterBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && !entity.getType().is(FTBOceanMobsTags.Entity.RIFT_MOBS) && entity.tickCount % 10 == 0) {
            livingEntity.addEffect(new MobEffectInstance(ModMobEffects.DROWNING_SHADOWS_EFFECT, 600, 0));
        }
    }
}
