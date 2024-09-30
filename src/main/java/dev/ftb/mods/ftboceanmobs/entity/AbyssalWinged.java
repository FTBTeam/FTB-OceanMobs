package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class AbyssalWinged extends FlyingMob implements Enemy {
    public AbyssalWinged(EntityType<? extends AbyssalWinged> entityType, Level level) {
        super(entityType, level);
    }
}
