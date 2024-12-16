package dev.ftb.mods.ftboceanmobs.mobeffects;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

import java.util.Set;

public class DrowningShadowsEffect extends MobEffect {
    public DrowningShadowsEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().magic(), (float)(1 << amplifier));

        boolean cured = livingEntity.getBlockStateOn().is(FTBOceanMobsTags.Blocks.DROWNING_SHADOWS_CURE);
        if (cured && livingEntity.level().isClientSide) {
            livingEntity.playSound(SoundEvents.SPONGE_ABSORB);
            Vec3 vec = livingEntity.getEyePosition().add(livingEntity.getLookAngle().normalize());
            livingEntity.level().addParticle(ParticleTypes.HEART, vec.x, vec.y, vec.z, 0, 0.01, 0);
        }

        return !cured;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // no milk bucket or honey cure
        cures.add(EffectCures.PROTECTED_BY_TOTEM);
    }
}
