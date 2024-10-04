package dev.ftb.mods.ftboceanmobs.client;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.client.render.AbyssalWingedRenderer;
import dev.ftb.mods.ftboceanmobs.client.render.CorrosiveCraigRenderer;
import dev.ftb.mods.ftboceanmobs.client.render.RiftlingObserverRenderer;
import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {
    public static void onModConstruction(ModContainer modContainer, IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        FTBOceanMobs.LOGGER.info("HELLO FROM CLIENT SETUP");
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.RIFTLING_OBSERVER.get(), RiftlingObserverRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ABYSSAL_WINGED.get(), ctx -> AbyssalWingedRenderer.scaled(ctx, 0.25f));
        event.registerEntityRenderer(ModEntityTypes.CORROSIVE_CRAIG.get(), ctx -> CorrosiveCraigRenderer.scaled(ctx, 1f));

        // TODO when we get some actual entity models...
        event.registerEntityRenderer(ModEntityTypes.MOSSBACK_GOLIATH.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ABYSSAL_SLUDGE.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SHADOW_BEAST.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIFT_MINOTAUR.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TENTACLED_HORROR.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIFT_DEMON.get(), DummyRenderer::new);
    }
}
