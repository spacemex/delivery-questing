package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record AcceptEmailJobPayload(UUID emailId) implements CustomPacketPayload {
    public static final Type<AcceptEmailJobPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "accept_email_job"));
    public static final StreamCodec<ByteBuf, AcceptEmailJobPayload> CODEC =
            UUIDUtil.STREAM_CODEC.map(AcceptEmailJobPayload::new, AcceptEmailJobPayload::emailId);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}