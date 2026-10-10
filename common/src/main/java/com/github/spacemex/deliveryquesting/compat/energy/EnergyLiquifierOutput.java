
package com.github.spacemex.deliveryquesting.compat.energy;

import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class EnergyLiquifierOutput {

    private EnergyLiquifierOutput() {
    }

    @ExpectPlatform
    public static void push(Level level, BlockPos pos, EnergyLiquifierBlockEntity machine) {
        throw new AssertionError();
    }
}
