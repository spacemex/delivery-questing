package com.github.spacemex.deliveryquesting.networking.packets;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record BuyOfferPayload(Identifier offerId) implements CustomPacketPayload {

    public static final Type<BuyOfferPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "buy_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyOfferPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            BuyOfferPayload::offerId,
            BuyOfferPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}