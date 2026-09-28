package com.github.spacemex.deliveryquesting.networking.packets;

import com.google.gson.Gson;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.github.spacemex.deliveryquesting.DeliveryQuesting.MOD_ID;

public record SyncPayload(String json) implements CustomPacketPayload {

    private static final Gson GSON = new Gson();

    public static final Type<SyncPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "sync_payload"
                    )
            );

    public static final StreamCodec<
            ? super RegistryFriendlyByteBuf,
            SyncPayload
            > CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPayload::json,
            SyncPayload::new
    );

    /**
     * Convenience constructor.
     * <p>
     * Converts the config map into valid JSON before transmission.
     */
    public SyncPayload(Map<String, Object> config) {
        this(GSON.toJson(config));
    }

    /**
     * Converts the received JSON back into a config map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> config() {
        Map<String, Object> result =
                GSON.fromJson(json, HashMap.class);

        if (result == null) {
            return new HashMap<>();
        }

        return result;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}