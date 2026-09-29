package dev.ftb.mods.ftboceanmobs.mobeffects;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.registry.ModMobEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class DrowningShadowsEffect extends MobEffect {
    public DrowningShadowsEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        livingEntity.hurtServer(level, livingEntity.damageSources().magic(), (float)(1 << amplifier));

        boolean cured = livingEntity.getBlockStateOn().is(FTBOceanMobsTags.Blocks.DROWNING_SHADOWS_CURE);
        if (cured) {
            livingEntity.playSound(SoundEvents.SPONGE_ABSORB);
            Vec3 vec = livingEntity.getEyePosition().add(livingEntity.getLookAngle().normalize());
            level.sendParticles(ParticleTypes.HEART, vec.x, vec.y, vec.z, 0, 0, 0.01, 0, 1.0);
        }

        return !cured;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @EventBusSubscriber(modid = FTBOceanMobs.MODID)
    public static class Listener {
        @SubscribeEvent
        public static void onEffectRemove(MobEffectEvent.Remove event) {
            if (event.getEffect().is(ModMobEffects.DROWNING_SHADOWS_EFFECT.getKey()) && isFinishingClearAllConsumable(event.getEntity())) {
                event.setCanceled(true);
            }
        }

        private static boolean isFinishingClearAllConsumable(LivingEntity entity) {
            if (!entity.isUsingItem()) {
                return false;
            }
            Consumable consumable = entity.getUseItem().get(DataComponents.CONSUMABLE);
            return consumable != null && consumable.onConsumeEffects().stream()
                    .anyMatch(effect -> effect instanceof ClearAllStatusEffectsConsumeEffect);
        }
    }
}
