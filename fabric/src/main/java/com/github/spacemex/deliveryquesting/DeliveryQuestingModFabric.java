package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.compat.energy.fabric.DronePadEnergyCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DeliveryQuestingModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        DeliveryQuesting.initialize();

        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            DronePadEnergyCompat.register();

            DeliveryQuesting.LOGGER.info("Enabled Team Reborn Energy integration");
        }
    }
}
