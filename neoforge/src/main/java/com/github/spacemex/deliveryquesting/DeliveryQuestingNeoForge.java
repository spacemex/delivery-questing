package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.compat.energy.neoforge.DronePadEnergyCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DeliveryQuesting.MOD_ID)
public final class DeliveryQuestingNeoForge {

    public DeliveryQuestingNeoForge(IEventBus eventBus) {
        DronePadEnergyCompat.register(eventBus);

        DeliveryQuesting.initialize();
    }
}
