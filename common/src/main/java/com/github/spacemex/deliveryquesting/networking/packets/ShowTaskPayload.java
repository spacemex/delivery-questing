package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ShowTaskPayload(Identifier taskId) implements CustomPacketPayload {
    public static final Type<ShowTaskPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "show_task"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowTaskPayload> CODEC =
            StreamCodec.composite(Identifier.STREAM_CODEC, ShowTaskPayload::taskId, ShowTaskPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}