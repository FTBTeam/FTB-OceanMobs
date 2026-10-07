package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RandomAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public RandomAttackableTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable TargetingConditions.Selector targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    protected void findTarget() {
        target = null;
        ServerLevel level = getServerLevel(mob);
        List<T> entities = mob.level().getEntitiesOfClass(targetType, getTargetSearchArea(getFollowDistance()), e -> true).stream()
                .filter(e -> targetConditions.test(level, mob, e))
                .toList();
        if (!entities.isEmpty()) {
            target = entities.get(mob.level().getRandom().nextInt(entities.size()));
            FTBOceanMobs.LOGGER.debug("set target: {}", target);
        }
    }
}
