
package com.github.spacemex.deliveryquesting.compat.energy.fabric;

import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class EnergyLiquifierOutputImpl {

    private EnergyLiquifierOutputImpl() {
    }

    public static void push(Level level, BlockPos pos, EnergyLiquifierBlockEntity machine) {
        if (!FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            return;
        }

        EnergyLiquifierEnergyCompat.pushEnergy(level, pos, machine);
    }
}
