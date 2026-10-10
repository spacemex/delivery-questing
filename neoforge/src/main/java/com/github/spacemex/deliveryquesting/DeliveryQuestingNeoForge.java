package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.compat.energy.neoforge.DronePadEnergyCompat;
import com.github.spacemex.deliveryquesting.compat.energy.neoforge.PackagerEnergyCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DeliveryQuesting.MOD_ID)
public final class DeliveryQuestingNeoForge {

    public DeliveryQuestingNeoForge(IEventBus eventBus) {
        DronePadEnergyCompat.register(eventBus);
        PackagerEnergyCompat.register(eventBus);

        DeliveryQuesting.initialize();
    }
}
