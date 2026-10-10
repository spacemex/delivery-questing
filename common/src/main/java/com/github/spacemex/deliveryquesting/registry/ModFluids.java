package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.fluid.LiquidEnergyFluidFactory;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public final class ModFluids {

    public static final Identifier LIQUID_ENERGY_ID =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "liquid_energy");

    private ModFluids() {
    }

    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.FLUID);

    public static final RegistrySupplier<Fluid> LIQUID_ENERGY;
    public static final RegistrySupplier<Fluid> LIQUID_ENERGY_FLOWING;

    public static void initialize() {
        FLUIDS.register();
    }

    static {
        LIQUID_ENERGY = FLUIDS.register("liquid_energy", LiquidEnergyFluidFactory::createSource);
        LIQUID_ENERGY_FLOWING = FLUIDS.register("liquid_energy_flowing", LiquidEnergyFluidFactory::createFlowing);
    }
}
