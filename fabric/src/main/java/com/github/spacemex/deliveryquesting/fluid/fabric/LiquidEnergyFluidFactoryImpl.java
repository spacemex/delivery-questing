package com.github.spacemex.deliveryquesting.fluid.fabric;

import net.minecraft.world.level.material.Fluid;

public final class LiquidEnergyFluidFactoryImpl {

    private LiquidEnergyFluidFactoryImpl() {
    }

    public static Fluid createSource() {
        return new LiquidEnergyFluid.Source();
    }

    public static Fluid createFlowing() {
        return new LiquidEnergyFluid.Flowing();
    }
}