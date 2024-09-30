package dev.ftb.mods.ftboceanmobs.registry;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS
            = DeferredRegister.createItems(FTBOceanMobs.MODID);

    private static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();

    public static final DeferredItem<Item> RIFTLING_OBSERVER_SPAWN_EGG
            = registerSpawnEgg("riftling_observer", ModEntityTypes.RIFTLING_OBSERVER, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> ABYSSAL_WINGED_SPAWN_EGG
            = registerSpawnEgg("abyssal_winged", ModEntityTypes.ABYSSAL_WINGED, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> CORROSIVE_CRAIG_SPAWN_EGG
            = registerSpawnEgg("corrosive_craig", ModEntityTypes.CORROSIVE_CRAIG, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> MOSSBACK_GOLIATH_SPAWN_EGG
            = registerSpawnEgg("mossback_goliath", ModEntityTypes.MOSSBACK_GOLIATH, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> ABYSSAL_SLUDGE_SPAWN_EGG
            = registerSpawnEgg("abyssal_sludge", ModEntityTypes.ABYSSAL_SLUDGE, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> SHADOW_BEAST_SPAWN_EGG
            = registerSpawnEgg("shadow_beast", ModEntityTypes.SHADOW_BEAST, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> RIFT_MINOTAUR_SPAWN_EGG
            = registerSpawnEgg("rift_minotaur", ModEntityTypes.RIFT_MINOTAUR, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> TENTACLED_HORROR_SPAWN_EGG
            = registerSpawnEgg("tentacled_horror", ModEntityTypes.TENTACLED_HORROR, 0xFFFF80FF, 0xFF20FF80);
    public static final DeferredItem<Item> RIFT_DEMON_SPAWN_EGG
            = registerSpawnEgg("rift_demon", ModEntityTypes.RIFT_DEMON, 0xFFFF80FF, 0xFF20FF80);

    public static List<DeferredItem<Item>> getSpawnEggs() {
        return Collections.unmodifiableList(SPAWN_EGGS);
    }

    private static DeferredItem<Item> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int bgColor, int hiColor) {
        DeferredItem<Item> egg = ITEMS.register(name + "_spawn_egg",  () -> new DeferredSpawnEggItem(type, bgColor, hiColor, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }
}
