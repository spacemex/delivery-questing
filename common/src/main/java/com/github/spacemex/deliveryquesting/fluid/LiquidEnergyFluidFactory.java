package com.github.spacemex.deliveryquesting.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.Fluid;

public final class LiquidEnergyFluidFactory {

    private LiquidEnergyFluidFactory() {
    }

    @ExpectPlatform
    public static Fluid createSource() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Fluid createFlowing() {
        throw new AssertionError();
    }
}