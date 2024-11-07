package dev.ftb.mods.ftboceanmobs.network;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum PlayerAttackTentaclePacket implements CustomPacketPayload {
    INSTANCE;

    public static final StreamCodec<FriendlyByteBuf, PlayerAttackTentaclePacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static final Type<PlayerAttackTentaclePacket> TYPE = new Type<>(FTBOceanMobs.id("player_attack_tentacle"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(PlayerAttackTentaclePacket ignoredPacket, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp && sp.getVehicle() instanceof TentacledHorror horror) {
            sp.attack(horror);
        }
    }
}
