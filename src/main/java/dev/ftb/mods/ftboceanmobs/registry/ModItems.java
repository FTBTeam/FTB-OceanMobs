package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS
            = DeferredRegister.createItems(FTBOceanMobs.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FTBOceanMobs.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("oceanmobs_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("ftboceanmobs.itemGroup.tab"))
            .icon(() -> new ItemStack(ModItems.SLUDGE_BALL.get()))
            .displayItems((parameters, output) -> {
                for (DeferredHolder<Item, ? extends Item> entry : ModItems.ITEMS.getEntries()) {
                    if (!(entry.get() instanceof SpawnEggItem)) {
                        output.accept(new ItemStack(entry.get()));
                    }
                }
            }).build());

    private static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();

    public static final DeferredItem<Item> RIFTLING_OBSERVER_SPAWN_EGG
            = registerSpawnEgg("riftling_observer", ModEntityTypes.RIFTLING_OBSERVER);
    public static final DeferredItem<Item> ABYSSAL_WINGED_SPAWN_EGG
            = registerSpawnEgg("abyssal_winged", ModEntityTypes.ABYSSAL_WINGED);
    public static final DeferredItem<Item> CORROSIVE_CRAIG_SPAWN_EGG
            = registerSpawnEgg("corrosive_craig", ModEntityTypes.CORROSIVE_CRAIG);
    public static final DeferredItem<Item> MOSSBACK_GOLIATH_SPAWN_EGG
            = registerSpawnEgg("mossback_goliath", ModEntityTypes.MOSSBACK_GOLIATH);
    public static final DeferredItem<Item> ABYSSAL_SLUDGE_SPAWN_EGG
            = registerSpawnEgg("abyssal_sludge", ModEntityTypes.ABYSSAL_SLUDGE);
    public static final DeferredItem<Item> SLUDGELING_SPAWN_EGG
            = registerSpawnEgg("sludgeling", ModEntityTypes.SLUDGELING);
    public static final DeferredItem<Item> SHADOW_BEAST_SPAWN_EGG
            = registerSpawnEgg("shadow_beast", ModEntityTypes.SHADOW_BEAST);
    public static final DeferredItem<Item> RIFT_MINOTAUR_SPAWN_EGG
            = registerSpawnEgg("rift_minotaur", ModEntityTypes.RIFT_MINOTAUR);
    public static final DeferredItem<Item> TENTACLED_HORROR_SPAWN_EGG
            = registerSpawnEgg("tentacled_horror", ModEntityTypes.TENTACLED_HORROR);
    public static final DeferredItem<Item> RIFT_DEMON_SPAWN_EGG
            = registerSpawnEgg("rift_demon", ModEntityTypes.RIFT_DEMON);
    public static final DeferredItem<Item> RIFT_WEAVER_SPAWN_EGG
            = registerSpawnEgg("rift_weaver", ModEntityTypes.RIFT_WEAVER);

    public static final DeferredItem<Item> SLUDGE_BALL
            = ITEMS.registerSimpleItem("sludge_ball");

    public static final DeferredItem<BucketItem> ABYSSAL_WATER_BUCKET
            = ITEMS.registerItem("abyssal_water_bucket", props -> new BucketItem(ModFluids.ABYSSAL_WATER.get(), props), ModItems::filledBucketProps);

    static {
        ITEMS.registerSimpleBlockItem("energy_geyser", ModBlocks.ENERGY_GEYSER);
        ITEMS.registerSimpleBlockItem("sludge_block", ModBlocks.SLUDGE_BLOCK);
    }

    public static List<DeferredItem<Item>> getSpawnEggs() {
        return Collections.unmodifiableList(SPAWN_EGGS);
    }

    private static DeferredItem<Item> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type) {
        DeferredItem<Item> egg = ITEMS.registerItem(name + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(type.get())));
        SPAWN_EGGS.add(egg);
        return egg;
    }

    public static Item.Properties filledBucketProps() {
        return new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET);
    }
}
