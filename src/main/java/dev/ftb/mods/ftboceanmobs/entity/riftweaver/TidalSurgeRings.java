package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TidalSurgeRings {
    static final int N_RINGS = 30;  // must be less than the animation time for the tidal surge animation
    private static final double TWO_PI = Math.PI * 2;
    private static final List<List<BlockPos>> RING_OFFSETS = Util.make(new ArrayList<>(N_RINGS), list -> {
        for (int i = 0; i < N_RINGS; i++) {
            list.add(new ArrayList<>());
        }
    });

    static List<BlockPos> getRingOffsets(int radius) {
        if (radius >= N_RINGS) return List.of();

        if (RING_OFFSETS.get(radius).isEmpty()) {
            if (radius == 0) {
                // degenerate case #1
                RING_OFFSETS.getFirst().add(BlockPos.ZERO);
            } else if (radius == 1) {
                // degenerate case #2
                RING_OFFSETS.get(1).addAll(List.of(
                        new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1),
                        new BlockPos(0, 0, 1), new BlockPos(1, 0, 0))
                );
            } else {
                int nPos = (int) (radius * Math.PI * 2);
                double incr = TWO_PI / nPos;
                for (double i = 0; i < TWO_PI; i += incr) {
                    Vec3 offset = new Vec3(Math.cos(i) * radius, 0.0, Math.sin(i) * radius);
                    RING_OFFSETS.get(radius).add(BlockPos.containing(offset));
                }
            }
        }
        return RING_OFFSETS.get(radius);
    }
}
