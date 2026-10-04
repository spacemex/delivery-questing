package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SubmitDroneDeliveryPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<SubmitDroneDeliveryPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "submit_drone_delivery"));
    public static final StreamCodec<ByteBuf, SubmitDroneDeliveryPayload> CODEC =
            BlockPos.STREAM_CODEC.map(SubmitDroneDeliveryPayload::new, SubmitDroneDeliveryPayload::pos);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}