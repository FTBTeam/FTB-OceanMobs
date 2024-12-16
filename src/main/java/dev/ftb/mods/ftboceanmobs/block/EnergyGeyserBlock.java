package dev.ftb.mods.ftboceanmobs.block;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnergyGeyserBlock extends Block {
    private static final VoxelShape COLLISION = box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    public EnergyGeyserBlock(Properties props) {
        super(props);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Vec3 vec = Vec3.atBottomCenterOf(pos.above());
        level.addParticle(ParticleTypes.DRAGON_BREATH, vec.x + random.nextFloat() * 0.5f - 0.25f, vec.y, vec.z + random.nextFloat() * 0.5f - 0.25f, 0f, 0.05f, 0f);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && !entity.getType().is(FTBOceanMobsTags.Entity.RIFT_MOBS)) {
            livingEntity.addEffect(new MobEffectInstance(ModMobEffects.DROWNING_SHADOWS_EFFECT, 60, 1));
        }
    }
}
