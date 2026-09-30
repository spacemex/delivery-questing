package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record AcceptEmailContractPayload(UUID emailId) implements CustomPacketPayload {
    public static final Type<AcceptEmailContractPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "accept_email_contract"));
    public static final StreamCodec<ByteBuf, AcceptEmailContractPayload> CODEC =
            UUIDUtil.STREAM_CODEC.map(AcceptEmailContractPayload::new, AcceptEmailContractPayload::emailId);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}