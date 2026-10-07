package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.networking.packets.SyncPayload;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

public final class ConfigSyncHandler {

    private static boolean initialized;

    private ConfigSyncHandler() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        NetworkManager.registerS2CPayloadType(SyncPayload.TYPE, SyncPayload.CODEC);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            SyncPayload payload = new SyncPayload(ConfigReader.getRawLocal());

            NetworkManager.sendToPlayer(serverPlayer, payload);
        });
    }
}