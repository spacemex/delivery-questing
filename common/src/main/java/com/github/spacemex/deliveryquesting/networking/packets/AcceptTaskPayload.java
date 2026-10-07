package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record AcceptTaskPayload(Identifier taskId) implements CustomPacketPayload {

    public static final Type<AcceptTaskPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "accept_task"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AcceptTaskPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            AcceptTaskPayload::taskId,
            AcceptTaskPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
