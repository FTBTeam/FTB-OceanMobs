package dev.ftb.mods.ftboceanmobs.mobai;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RandomAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public RandomAttackableTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    protected void findTarget() {
        List<T> entities = mob.level().getEntitiesOfClass(targetType, getTargetSearchArea(getFollowDistance()), e -> true).stream()
                .filter(e -> targetConditions.test(mob, e))
                .toList();
        if (!entities.isEmpty()) {
            target = entities.get(mob.level().random.nextInt(entities.size()));
            FTBOceanMobs.LOGGER.debug("set target: {}", target);
        }
    }
}
