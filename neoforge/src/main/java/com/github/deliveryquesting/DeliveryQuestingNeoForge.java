package com.github.deliveryquesting;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DeliveryQuesting.MOD_ID)
public final class DeliveryQuestingNeoForge {

    public DeliveryQuestingNeoForge(IEventBus eventBus) {
        DeliveryQuesting.initialize();
    }
}
