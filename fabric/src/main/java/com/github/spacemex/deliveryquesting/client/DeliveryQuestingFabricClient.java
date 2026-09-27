package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.fabricmc.api.ClientModInitializer;

public class DeliveryQuestingFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DeliveryQuesting.initializeClientOnly();
    }
}
