package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.fabricmc.api.ModInitializer;

public class DeliveryQuestingModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        DeliveryQuesting.initialize();
    }
}
