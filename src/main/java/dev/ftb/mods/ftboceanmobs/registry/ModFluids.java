package dev.ftb.mods.ftboceanmobs.registry;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.fluid.AbyssalWaterFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, FTBOceanMobs.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, FTBOceanMobs.MODID);

    public static final Supplier<FluidType> ABYSSAL_WATER_TYPE = registerFluidType("abyssal_water",
            standardProps(1000, 1000));

    public static final Supplier<FlowingFluid> ABYSSAL_WATER = register("abyssal_water", AbyssalWaterFluid.Source::new);
    public static final Supplier<FlowingFluid> ABYSSAL_WATER_FLOWING = register("abyssal_water_flowing", AbyssalWaterFluid.Flowing::new);

    //--------------------------

    private static <T extends Fluid> Supplier<T> register(String name, final Supplier<T> sup) {
        return FLUIDS.register(name, sup);
    }

    private static Supplier<FluidType> registerFluidType(String name, FluidType.Properties props) {
        return FLUID_TYPES.register(name, () -> new FluidType(props));
    }

    private static FluidType.Properties standardProps(int density, int viscosity) {
        return FluidType.Properties.create()
                .density(density)
                .viscosity(viscosity)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }
}
