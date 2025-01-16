package dev.ftb.mods.ftboceanmobs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = FTBOceanMobs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RIFT_WEAVER_ARENA_RADIUS = BUILDER
            .comment("Rift Weaver boss arena size (how far horizontally will the boss roam from its start pos")
            .defineInRange("rift_weaver_arena_size", 48, 16, 128);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int arenaRadius;
    public static int arenaRadiusSq;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        arenaRadius = RIFT_WEAVER_ARENA_RADIUS.get();
        arenaRadiusSq = arenaRadius * arenaRadius;
    }
}
