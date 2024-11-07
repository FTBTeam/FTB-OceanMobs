package dev.ftb.mods.ftboceanmobs.network;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FTBOceanMobs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String NETWORK_VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(FTBOceanMobs.MODID)
                .versioned(NETWORK_VERSION);

        // serverbound
        registrar.playToServer(PlayerAttackTentaclePacket.TYPE, PlayerAttackTentaclePacket.STREAM_CODEC, PlayerAttackTentaclePacket::handleData);
    }
}
