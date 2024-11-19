package dev.ftb.mods.ftboceanmobs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = FTBOceanMobs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {

    }
}
