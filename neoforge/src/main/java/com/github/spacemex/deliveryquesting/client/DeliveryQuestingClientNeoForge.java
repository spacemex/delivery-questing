package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = DeliveryQuesting.MOD_ID, dist = Dist.CLIENT)
public final class DeliveryQuestingClientNeoForge {

    public DeliveryQuestingClientNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::clientInitialization);
    }

    private void clientInitialization(FMLClientSetupEvent event) {
        DeliveryQuesting.initializeClientOnly();
    }
}
