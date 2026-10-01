package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.task.OfferDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ComputerOfferEntry(Identifier id, Identifier item, int count, long price, int minLevel,
                                 boolean forEveryMember, boolean unlocked, boolean affordable) {
    private static final int MAX_OFFERS = 4096;

    public static ComputerOfferEntry from(OfferDefinition offer, DeliveryGroup group) {
        return new ComputerOfferEntry(
                offer.id(),
                offer.item(),
                offer.count(),
                offer.price(),
                offer.minLevel(),
                offer.forEveryMember(),
                group.wholeLevel() >= offer.minLevel(),
                group.balance() >= offer.price()
        );
    }

    public boolean canBuy() {
        return unlocked && affordable;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(id.toString(), 256);
        buffer.writeUtf(item.toString(), 256);
        buffer.writeVarInt(count);
        buffer.writeLong(price);
        buffer.writeVarInt(minLevel);
        buffer.writeBoolean(forEveryMember);
        buffer.writeBoolean(unlocked);
        buffer.writeBoolean(affordable);
    }

    public static ComputerOfferEntry read(FriendlyByteBuf buffer) {
        return new ComputerOfferEntry(
                Identifier.parse(buffer.readUtf(256)),
                Identifier.parse(buffer.readUtf(256)),
                buffer.readVarInt(),
                buffer.readLong(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void writeList(FriendlyByteBuf buffer, List<ComputerOfferEntry> offers) {
        if (offers.size() > MAX_OFFERS) {
            throw new IllegalArgumentException("Too many Computer offers: " + offers.size());
        }

        buffer.writeVarInt(offers.size());

        for (ComputerOfferEntry offer : offers) {
            offer.write(buffer);
        }
    }

    public static List<ComputerOfferEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_OFFERS) {
            throw new IllegalStateException("Invalid Computer offer count: " + size);
        }

        List<ComputerOfferEntry> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(read(buffer));
        }

        return List.copyOf(result);
    }
}