package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

public class Sludgeling extends Slime {
    private static final TargetingConditions SLUDGE_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();

    public Sludgeling(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.5);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(1, false);

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.36);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.5);
        if (resetHealth) {
            setHealth(getMaxHealth());
        }
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    protected boolean isDealsDamage() {
        return true;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ModParticleTypes.SLUDGE.get();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (tickCount % 40 == 0) {
            if (level.getNearbyEntities(AbyssalSludge.class, SLUDGE_TARGETING, this, getBoundingBox().inflate(32)).isEmpty()) {
                kill(level);
            }
        }
    }
}
