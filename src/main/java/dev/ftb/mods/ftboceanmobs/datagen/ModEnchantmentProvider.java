package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.List;
import java.util.Optional;

public class ModEnchantmentProvider {
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        context.register(FTBOceanMobs.RIFT_DISRUPTOR_ENCHANTMENT,
                new Enchantment(
                        Component.translatable("enchantment.ftboceanmobs.rift_disruptor"),
                        new Enchantment.EnchantmentDefinition(
                            context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                Optional.of(context.lookup(Registries.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE)),
                                5,
                                5,
                                new Enchantment.Cost(5, 8),
                                new Enchantment.Cost(25, 8),
                                2,
                                List.of(EquipmentSlotGroup.MAINHAND)
                        ),
                        context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE),
                        DataComponentMap.builder()
                                .set(EnchantmentEffectComponents.DAMAGE, List.of(new ConditionalEffect<>(
                                        new AddValue(LevelBasedValue.perLevel(2.5F)),
                                        Optional.of(LootItemEntityPropertyCondition.hasProperties(
                                                LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                                                        .entityType(EntityTypePredicate.of(FTBOceanMobsTags.Entity.RIFT_MOBS))
                                        ).build())
                                ))).build()
                )
        );
    }
}
