package dev.ftb.mods.ftboceanmobs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;

public class MiscUtil {
    public static boolean canPathfindToTarget(Mob mob, LivingEntity target, float dist) {
        Path path = mob.getNavigation().createPath(target, 0);
        return path != null && path.getDistToTarget() < dist;
    }
}
