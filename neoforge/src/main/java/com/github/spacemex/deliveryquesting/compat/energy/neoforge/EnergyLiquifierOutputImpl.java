
package com.github.spacemex.deliveryquesting.compat.energy.neoforge;

import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class EnergyLiquifierOutputImpl {

    private EnergyLiquifierOutputImpl() {
    }

    public static void push(Level level, BlockPos pos, EnergyLiquifierBlockEntity machine) {
        EnergyLiquifierCompat.pushEnergy(level, pos, machine);
    }
}
