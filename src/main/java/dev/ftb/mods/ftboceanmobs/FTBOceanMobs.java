package dev.ftb.mods.ftboceanmobs;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftboceanmobs.client.ClientSetup;
import dev.ftb.mods.ftboceanmobs.datagen.DataGenerators;
import dev.ftb.mods.ftboceanmobs.entity.*;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import dev.ftb.mods.ftboceanmobs.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FTBOceanMobs.MODID)
public class FTBOceanMobs
{
    public static final String MODID = "ftboceanmobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBOceanMobs(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.onModConstruction(modContainer, modEventBus);
        }

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addSpawnEggsToCreativeTab);
        modEventBus.addListener(this::registerEntityAttributes);

        registerAll(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.RIFTLING_OBSERVER.get(), RiftlingObserver.createAttributes().build());
        event.put(ModEntityTypes.ABYSSAL_WINGED.get(), AbyssalWinged.createAttributes().build());
        event.put(ModEntityTypes.CORROSIVE_CRAIG.get(), CorrosiveCraig.createAttributes().build());
        event.put(ModEntityTypes.MOSSBACK_GOLIATH.get(), MossbackGoliath.createAttributes().build());
        event.put(ModEntityTypes.ABYSSAL_SLUDGE.get(), AbyssalSludge.createAttributes().build());
        event.put(ModEntityTypes.SLUDGELING.get(), Sludgeling.createAttributes().build());
        event.put(ModEntityTypes.SHADOW_BEAST.get(), ShadowBeast.createAttributes().build());
        event.put(ModEntityTypes.RIFT_MINOTAUR.get(), RiftMinotaur.createAttributes().build());
        event.put(ModEntityTypes.TENTACLED_HORROR.get(), TentacledHorror.createAttributes().build());
        event.put(ModEntityTypes.RIFT_DEMON.get(), RiftDemon.createAttributes().build());
        event.put(ModEntityTypes.RIFT_WEAVER.get(), RiftWeaverBoss.createAttributes().build());
    }

    private void registerAll(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.CREATIVE_MODE_TABS.register(modBus);
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModParticleTypes.PARTICLES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModMobEffects.MOB_EFFECTS.register(modBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void addSpawnEggsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            ModItems.ITEMS.getEntries().forEach(entry -> {
                if (entry.get() instanceof SpawnEggItem egg) {
                    event.accept(egg);
                }
            });
        }
    }

    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
