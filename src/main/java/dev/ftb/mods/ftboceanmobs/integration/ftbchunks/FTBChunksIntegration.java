package dev.ftb.mods.ftboceanmobs.integration.ftbchunks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class FTBChunksIntegration {
    private static boolean chunksModLoaded;

    public static void init() {
        chunksModLoaded = ModList.get().isLoaded("ftbchunks");
    }

    public static boolean canMobGriefBlocks(Level level, BlockPos pos) {
        return !chunksModLoaded || FTBChunksAccess.canMobGriefBlocks(level, pos);
    }
}
