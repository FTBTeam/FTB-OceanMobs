package dev.ftb.mods.ftboceanmobs.client;

import dev.ftb.mods.ftboceanmobs.client.particle.ItemParticleProvider;
import dev.ftb.mods.ftboceanmobs.client.render.*;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import dev.ftb.mods.ftboceanmobs.network.PlayerAttackTentaclePacket;
import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import dev.ftb.mods.ftboceanmobs.registry.ModItems;
import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientSetup {
    public static void onModConstruction(ModContainer modContainer, IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerParticleProviders);

        NeoForge.EVENT_BUS.addListener(ClientSetup::onPlayerLeftClickEmpty);
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

        event.registerEntityRenderer(ModEntityTypes.SLUDGELING.get(), SludgelingRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TUMBLING_BLOCK.get(), TumblingBlockRenderer::new);

        // TODO when we get some actual entity models...
        event.registerEntityRenderer(ModEntityTypes.RIFT_DEMON.get(), DummyRenderer::new);
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
