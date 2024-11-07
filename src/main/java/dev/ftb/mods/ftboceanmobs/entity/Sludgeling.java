package dev.ftb.mods.ftboceanmobs.entity;

import dev.ftb.mods.ftboceanmobs.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

public class Sludgeling extends Slime {
    public Sludgeling(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.5);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(1, resetHealth);

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.36);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.5);
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
}
