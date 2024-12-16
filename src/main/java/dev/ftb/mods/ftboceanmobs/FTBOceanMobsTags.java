package dev.ftb.mods.ftboceanmobs;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class FTBOceanMobsTags {
    public static class Blocks {
        public static final TagKey<Block> SEISMIC_SMASHABLE = modTag("seismic_smashable");
        public static final TagKey<Block> SEISMIC_CRACKED = modTag("seismic_cracked");
        public static final TagKey<Block> SEISMIC_SMASHED = modTag("seismic_smashed");
        public static final TagKey<Block> DROWNING_SHADOWS_CURE = modTag("drowning_shadows_cure");

        private static TagKey<Block> modTag(String name) {
            return TagKey.create(Registries.BLOCK, FTBOceanMobs.id(name));
        }
    }

    public static class Entity {
        public static final TagKey<EntityType<?>> RIFT_MOBS = modTag("rift_mobs");

        private static TagKey<EntityType<?>> modTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, FTBOceanMobs.id(name));
        }

    }
}
