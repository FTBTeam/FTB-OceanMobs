package dev.ftb.mods.ftboceanmobs.datagen;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModTagsProvider {
    public static class EntityType extends EntityTypeTagsProvider {
        public EntityType(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, provider, FTBOceanMobs.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (var type : ModEntityTypes.ENTITY_TYPES.getEntries()) {
                if (type != ModEntityTypes.TUMBLING_BLOCK) {
                    tag(FTBOceanMobsTags.Entity.RIFT_MOBS).add(type.get());
                }
            }
        }
    }

    public static class Block extends BlockTagsProvider {
        public Block(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, FTBOceanMobs.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(FTBOceanMobsTags.Blocks.SEISMIC_SMASHABLE).add(Blocks.SANDSTONE);
            tag(FTBOceanMobsTags.Blocks.SEISMIC_CRACKED).add(Blocks.RED_SANDSTONE);
            tag(FTBOceanMobsTags.Blocks.SEISMIC_SMASHED).add(Blocks.SAND);

            tag(FTBOceanMobsTags.Blocks.DROWNING_SHADOWS_CURE).add(Blocks.SPONGE);
            tag(FTBOceanMobsTags.Blocks.DROWNING_SHADOWS_CURE).add(Blocks.WET_SPONGE);
        }
    }
}
