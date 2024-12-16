package dev.ftb.mods.ftboceanmobs.client;

import com.mojang.blaze3d.shaders.FogShape;
import dev.ftb.mods.ftboceanmobs.client.particle.ItemParticleProvider;
import dev.ftb.mods.ftboceanmobs.client.render.*;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import dev.ftb.mods.ftboceanmobs.fluid.AbyssalWaterFluid;
import dev.ftb.mods.ftboceanmobs.network.PlayerAttackTentaclePacket;
import dev.ftb.mods.ftboceanmobs.registry.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientSetup {
    public static void onModConstruction(ModContainer modContainer, IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerParticleProviders);
        modEventBus.addListener(ClientSetup::registerClientExtensions);

        NeoForge.EVENT_BUS.addListener(ClientSetup::onPlayerLeftClickEmpty);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onFogDensity);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onFogColor);
    }

    private static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getEntity() instanceof Player player && player.hasEffect(ModMobEffects.DROWNING_SHADOWS_EFFECT)) {
            event.setRed(0.26f);
            event.setGreen(0.05f);
            event.setBlue(0.3f);
        }
    }

    private static void onFogDensity(ViewportEvent.RenderFog event) {
        if (event.getCamera().getEntity() instanceof Player player && player.hasEffect(ModMobEffects.DROWNING_SHADOWS_EFFECT)) {
            int ticks = player.getEffect(ModMobEffects.DROWNING_SHADOWS_EFFECT).getDuration();
            event.setNearPlaneDistance(0.2f);
            event.setFarPlaneDistance(20.0f + (ticks < 100 ? (100 - ticks) * 1.2f : 0f));
            event.setFogShape(FogShape.SPHERE);
            event.setCanceled(true);
        }
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.RIFTLING_OBSERVER.get(), RiftlingObserverRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ABYSSAL_WINGED.get(), ctx -> AbyssalWingedRenderer.scaled(ctx, 1f));
        event.registerEntityRenderer(ModEntityTypes.CORROSIVE_CRAIG.get(), ctx -> CorrosiveCraigRenderer.scaled(ctx, 0.9f));
        event.registerEntityRenderer(ModEntityTypes.MOSSBACK_GOLIATH.get(), ctx -> MossbackGoliathRenderer.scaled(ctx, 0.75f));
        event.registerEntityRenderer(ModEntityTypes.ABYSSAL_SLUDGE.get(), ctx -> AbyssalSludgeRenderer.scaled(ctx,1f));
        event.registerEntityRenderer(ModEntityTypes.SHADOW_BEAST.get(), ctx -> ShadowBeastRenderer.scaled(ctx,0.25f));
        event.registerEntityRenderer(ModEntityTypes.RIFT_MINOTAUR.get(), ctx -> RiftMinotaurRenderer.scaled(ctx,1.17f));
        event.registerEntityRenderer(ModEntityTypes.TENTACLED_HORROR.get(), ctx -> TentacledHorrorRenderer.scaled(ctx, 2.9f));
        event.registerEntityRenderer(ModEntityTypes.RIFT_DEMON.get(), ctx -> RiftDemonRenderer.scaled(ctx, 1.18f));
        event.registerEntityRenderer(ModEntityTypes.RIFT_WEAVER.get(), ctx -> RiftWeaverBossRenderer.scaled(ctx, 2.9f));

        // other entities
        event.registerEntityRenderer(ModEntityTypes.SLUDGELING.get(), SludgelingRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TUMBLING_BLOCK.get(), TumblingBlockRenderer::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(AbyssalWaterFluid.RENDER_PROPS, ModFluids.ABYSSAL_WATER_TYPE.get());
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticleTypes.SLUDGE.get(), new ItemParticleProvider(ModItems.SLUDGE_BALL.toStack()));
        event.registerSpecial(ModParticleTypes.MOSSBACK_SHARD.get(), new ItemParticleProvider(Items.AMETHYST_SHARD.getDefaultInstance()));
        event.registerSpecial(ModParticleTypes.HORROR_INK.get(), new ItemParticleProvider(Items.BLACK_DYE.getDefaultInstance()));
    }

    private static void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().getVehicle() instanceof TentacledHorror) {
            PacketDistributor.sendToServer(PlayerAttackTentaclePacket.INSTANCE);
        }
    }
}
