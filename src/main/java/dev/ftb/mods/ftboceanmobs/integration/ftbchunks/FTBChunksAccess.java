package dev.ftb.mods.ftboceanmobs.integration.ftbchunks;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class FTBChunksAccess {
    static boolean canMobGriefBlocks(Level level, BlockPos pos) {
        ClaimedChunk cc = FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level, pos));
        return cc == null || cc.getTeamData().allowMobGriefing();
    }
}
