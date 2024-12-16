package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
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
            = registerSpawnEgg("riftling_observer", ModEntityTypes.RIFTLING_OBSERVER, 0xFF63267E, 0xFFF770DC);
    public static final DeferredItem<Item> ABYSSAL_WINGED_SPAWN_EGG
            = registerSpawnEgg("abyssal_winged", ModEntityTypes.ABYSSAL_WINGED, 0xFF462479, 0xFF903DDB);
    public static final DeferredItem<Item> CORROSIVE_CRAIG_SPAWN_EGG
            = registerSpawnEgg("corrosive_craig", ModEntityTypes.CORROSIVE_CRAIG, 0xFF332B56, 0xFFD23AFF);
    public static final DeferredItem<Item> MOSSBACK_GOLIATH_SPAWN_EGG
            = registerSpawnEgg("mossback_goliath", ModEntityTypes.MOSSBACK_GOLIATH, 0xFF1D1448, 0xFFFCE5FE);
    public static final DeferredItem<Item> ABYSSAL_SLUDGE_SPAWN_EGG
            = registerSpawnEgg("abyssal_sludge", ModEntityTypes.ABYSSAL_SLUDGE, 0xFF5A189E, 0xFFBB7FFF);
    public static final DeferredItem<Item> SLUDGELING_SPAWN_EGG
            = registerSpawnEgg("sludgeling", ModEntityTypes.SLUDGELING, 0xFF191134, 0xFF674DC4);
    public static final DeferredItem<Item> SHADOW_BEAST_SPAWN_EGG
            = registerSpawnEgg("shadow_beast", ModEntityTypes.SHADOW_BEAST, 0xFF241F3B, 0xFFE451FF);
    public static final DeferredItem<Item> RIFT_MINOTAUR_SPAWN_EGG
            = registerSpawnEgg("rift_minotaur", ModEntityTypes.RIFT_MINOTAUR, 0xFF520E87, 0xFFFF57FF);
    public static final DeferredItem<Item> TENTACLED_HORROR_SPAWN_EGG
            = registerSpawnEgg("tentacled_horror", ModEntityTypes.TENTACLED_HORROR, 0xFF342455, 0xFFA466BC);
    public static final DeferredItem<Item> RIFT_DEMON_SPAWN_EGG
            = registerSpawnEgg("rift_demon", ModEntityTypes.RIFT_DEMON, 0xFF220A40, 0xFFEFAA46);
    public static final DeferredItem<Item> RIFT_WEAVER_SPAWN_EGG
            = registerSpawnEgg("rift_weaver", ModEntityTypes.RIFT_WEAVER, 0xFF220A40, 0xFFEFAA46);

    public static final DeferredItem<Item> SLUDGE_BALL
            = ITEMS.register("sludge_ball", () -> new Item(new Item.Properties()));

    public static final DeferredItem<BucketItem> ABYSSAL_WATER_BUCKET
            = ITEMS.register("abyssal_water_bucket", () -> new BucketItem(ModFluids.ABYSSAL_WATER.get(), filledBucketProps()));

    static {
        ITEMS.registerSimpleBlockItem("energy_geyser", ModBlocks.ENERGY_GEYSER);
        ITEMS.registerSimpleBlockItem("sludge_block", ModBlocks.SLUDGE_BLOCK);
    }

    public static List<DeferredItem<Item>> getSpawnEggs() {
        return Collections.unmodifiableList(SPAWN_EGGS);
    }

    private static DeferredItem<Item> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int bgColor, int hiColor) {
        DeferredItem<Item> egg = ITEMS.register(name + "_spawn_egg",  () -> new DeferredSpawnEggItem(type, bgColor, hiColor, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }

    public static Item.Properties filledBucketProps() {
        return new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET);
    }
}
