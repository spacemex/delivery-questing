package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record CollectParcelPayload(UUID parcelId) implements CustomPacketPayload {
    public static final Type<CollectParcelPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "collect_parcel"));
    public static final StreamCodec<ByteBuf, CollectParcelPayload> CODEC =
            UUIDUtil.STREAM_CODEC.map(CollectParcelPayload::new, CollectParcelPayload::parcelId);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}