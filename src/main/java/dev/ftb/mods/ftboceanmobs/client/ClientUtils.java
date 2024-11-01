package dev.ftb.mods.ftboceanmobs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class ClientUtils {
    public static Optional<Player> getOptionalClientPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }
}
