package dev.ftb.mods.ftboceanmobs;

import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class RiftMobPlacement {
    static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerPlacement(event, ModEntityTypes.RIFTLING_OBSERVER.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.ABYSSAL_WINGED.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.CORROSIVE_CRAIG.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.MOSSBACK_GOLIATH.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.ABYSSAL_SLUDGE.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.SHADOW_BEAST.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.RIFT_MINOTAUR.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.TENTACLED_HORROR.get(), RiftMobPlacement::riftSpawnRulesCheck);
        registerPlacement(event, ModEntityTypes.RIFT_DEMON.get(), RiftMobPlacement::riftSpawnRulesCheck);

        registerPlacement(event, ModEntityTypes.SLUDGELING.get(), RiftMobPlacement::noNaturalSpawn);
        registerPlacement(event, ModEntityTypes.RIFT_WEAVER.get(), RiftMobPlacement::noNaturalSpawn);
    }

    private static <T extends Entity> void registerPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type, SpawnPlacements.SpawnPredicate<T> pred) {
        event.register(type, RIFT_PLACEMENT, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pred, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean riftSpawnRulesCheck(EntityType<? extends Monster> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }

    private static boolean noNaturalSpawn(EntityType<?> entityType, ServerLevelAccessor serverLevel, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return false;
    }

    private static final SpawnPlacementType RIFT_PLACEMENT = (level, pos, entityType) ->
            SpawnPlacementTypes.IN_WATER.isSpawnPositionOk(level, pos, entityType)
                    || SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, entityType);
}
