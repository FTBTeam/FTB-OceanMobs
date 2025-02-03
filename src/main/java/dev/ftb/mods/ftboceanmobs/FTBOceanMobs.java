package dev.ftb.mods.ftboceanmobs;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftboceanmobs.client.ClientSetup;
import dev.ftb.mods.ftboceanmobs.datagen.DataGenerators;
import dev.ftb.mods.ftboceanmobs.entity.*;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import dev.ftb.mods.ftboceanmobs.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

@Mod(FTBOceanMobs.MODID)
public class FTBOceanMobs {
    public static final String MODID = "ftboceanmobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Enchantment> RIFT_DISRUPTOR_ENCHANTMENT
            = ResourceKey.create(Registries.ENCHANTMENT, id("rift_disruptor"));

    public FTBOceanMobs(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.onModConstruction(modContainer, modEventBus);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(this::addSpawnEggsToCreativeTab);
        modEventBus.addListener(this::registerEntityAttributes);

        registerAll(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
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

    private void addSpawnEggsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            ModItems.ITEMS.getEntries().forEach(entry -> {
                if (entry.get() instanceof SpawnEggItem egg) {
                    event.accept(egg);
                }
            });
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
