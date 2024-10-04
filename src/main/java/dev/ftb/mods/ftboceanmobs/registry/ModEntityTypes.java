package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<RiftlingObserver> riftlingObserver() {
        return EntityType.Builder.of(RiftlingObserver::new, MobCategory.MONSTER)
                .sized(0.78F, 1.1F)
                .eyeHeight(0.72F)
                .ridingOffset(0.04F)
                .clientTrackingRange(8)
                .updateInterval(2);
    }

    private static EntityType.Builder<AbyssalWinged> abyssalWinged() {
        return EntityType.Builder.of(AbyssalWinged::new, MobCategory.MONSTER)
                .sized(0.7F, 0.9F)
                .eyeHeight(0.57F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<CorrosiveCraig> corrosiveCraig() {
        return EntityType.Builder.of(CorrosiveCraig::new, MobCategory.MONSTER)
                .sized(1.5F, 3.6F)
                .eyeHeight(3.05F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<MossbackGoliath> mossbackGoliath() {
        return EntityType.Builder.of(MossbackGoliath::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<AbyssalSludge> abyssalSludge() {
        return EntityType.Builder.of(AbyssalSludge::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<ShadowBeast> shadowBeast() {
        return EntityType.Builder.of(ShadowBeast::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<RiftMinotaur> riftMinotaur() {
        return EntityType.Builder.of(RiftMinotaur::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<RiftDemon> riftDemon() {
        return EntityType.Builder.of(RiftDemon::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }

    private static EntityType.Builder<TentacledHorror> tentacledHorror() {
        return EntityType.Builder.of(TentacledHorror::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .eyeHeight(0.175F)
                .passengerAttachments(0.3375F)
                .ridingOffset(-0.125F)
                .clientTrackingRange(8);
    }
}
