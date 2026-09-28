package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.networking.packets.SyncPayload;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;

public final class ClientConfigSyncHandler {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncPayload.TYPE, SyncPayload.CODEC,
                (payload, context) -> context.queue(() -> ConfigReader.setSyncedConfig(payload.config())));

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> ConfigReader.clearSyncedConfig());
    }
}
