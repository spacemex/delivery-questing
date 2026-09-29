package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import dev.architectury.event.events.common.LifecycleEvent;

public final class ProgressionManager {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        LifecycleEvent.SERVER_STARTING.register(server -> {
            DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(server);

            DeliveryQuesting.LOGGER.info("Loaded Delivery Questing progression with {} group(s)", data.groupCount());
        });
    }
}
