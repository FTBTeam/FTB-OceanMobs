package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.*;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, FTBOceanMobs.MODID);

    public static final Supplier<EntityType<RiftlingObserver>> RIFTLING_OBSERVER
            = register("riftling_observer", ModEntityTypes::riftlingObserver);
    public static final Supplier<EntityType<AbyssalWinged>> ABYSSAL_WINGED
            = register("abyssal_winged", ModEntityTypes::abyssalWinged);
    public static final Supplier<EntityType<CorrosiveCraig>> CORROSIVE_CRAIG
            = register("corrosive_craig", ModEntityTypes::corrosiveCraig);
    public static final Supplier<EntityType<MossbackGoliath>> MOSSBACK_GOLIATH
            = register("mossback_goliath", ModEntityTypes::mossbackGoliath);
    public static final Supplier<EntityType<AbyssalSludge>> ABYSSAL_SLUDGE
            = register("abyssal_sludge", ModEntityTypes::abyssalSludge);
    public static final Supplier<EntityType<ShadowBeast>> SHADOW_BEAST
            = register("shadow_beast", ModEntityTypes::shadowBeast);
    public static final Supplier<EntityType<RiftMinotaur>> RIFT_MINOTAUR
            = register("rift_minotaur", ModEntityTypes::riftMinotaur);
    public static final Supplier<EntityType<TentacledHorror>> TENTACLED_HORROR
            = register("tentacled_horror", ModEntityTypes::tentacledHorror);
    public static final Supplier<EntityType<RiftDemon>> RIFT_DEMON
            = register("rift_demon", ModEntityTypes::riftDemon);
    public static final Supplier<EntityType<RiftWeaverBoss>> RIFT_WEAVER
            = register("rift_weaver", ModEntityTypes::riftWeaver);

    public static final Supplier<EntityType<Sludgeling>> SLUDGELING
            = register("sludgeling", ModEntityTypes::sludgeling);
    public static final Supplier<EntityType<TumblingBlockEntity>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntityTypes::tumblingBlock);

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<RiftlingObserver> riftlingObserver() {
        return EntityType.Builder.of(RiftlingObserver::new, MobCategory.MONSTER)
                .sized(0.78F, 1.1F)
                .eyeHeight(0.82F)
                .clientTrackingRange(10)
                .updateInterval(2);
    }

    private static EntityType.Builder<AbyssalWinged> abyssalWinged() {
        return EntityType.Builder.of(AbyssalWinged::new, MobCategory.MONSTER)
                .sized(2.3F, 3.0F)
                .eyeHeight(2.25F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<CorrosiveCraig> corrosiveCraig() {
        return EntityType.Builder.of(CorrosiveCraig::new, MobCategory.MONSTER)
                .sized(1.55F, 3.2F)
                .eyeHeight(2.85F)
                .clientTrackingRange(10)
                .fireImmune();
    }

    private static EntityType.Builder<MossbackGoliath> mossbackGoliath() {
        return EntityType.Builder.of(MossbackGoliath::new, MobCategory.MONSTER)
                .sized(1.05F, 2.5F)
                .eyeHeight(2.03F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<AbyssalSludge> abyssalSludge() {
        return EntityType.Builder.of(AbyssalSludge::new, MobCategory.MONSTER)
                .sized(1.4F, 2.9F)
                .eyeHeight(2.25F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<ShadowBeast> shadowBeast() {
        return EntityType.Builder.of(ShadowBeast::new, MobCategory.MONSTER)
                .sized(0.75F, 1.25F)
                .eyeHeight(1.08F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<RiftMinotaur> riftMinotaur() {
        return EntityType.Builder.of(RiftMinotaur::new, MobCategory.MONSTER)
                .sized(1.6F, 3.4F)
                .eyeHeight(3.1F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<TentacledHorror> tentacledHorror() {
        return EntityType.Builder.of(TentacledHorror::new, MobCategory.MONSTER)
                .sized(2.5F, 9F)
                .eyeHeight(7.1F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<RiftDemon> riftDemon() {
        return EntityType.Builder.of(RiftDemon::new, MobCategory.MONSTER)
                .sized(1.75F, 3.75F)
                .eyeHeight(3.4F)
                .clientTrackingRange(10)
                .fireImmune();
    }

    private static EntityType.Builder<RiftWeaverBoss> riftWeaver() {
        return EntityType.Builder.of(RiftWeaverBoss::new, MobCategory.MONSTER)
                .sized(8F, 15F)
                .eyeHeight(15F)
                .clientTrackingRange(10)
                .fireImmune();
    }

    private static EntityType.Builder<Sludgeling> sludgeling() {
        return EntityType.Builder.of(Sludgeling::new, MobCategory.MONSTER)
                .sized(0.52F, 0.52F)
                .eyeHeight(0.325F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<TumblingBlockEntity> tumblingBlock() {
        return EntityType.Builder.<TumblingBlockEntity>of(TumblingBlockEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(true);
    }
}
