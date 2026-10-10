package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.compat.energy.fabric.DronePadEnergyCompat;
import com.github.spacemex.deliveryquesting.compat.energy.fabric.PackagerEnergyCompat;
import com.github.spacemex.deliveryquesting.compat.fluid.fabric.BarrelFluidCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DeliveryQuestingModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        DeliveryQuesting.initialize();

        BarrelFluidCompat.register();

        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            DronePadEnergyCompat.register();
            PackagerEnergyCompat.register();

            DeliveryQuesting.LOGGER.info("Enabled Team Reborn Energy integration");
        }
    }
}
