package com.github.spacemex.deliveryquesting.menu.entry;

import com.github.spacemex.deliveryquesting.progression.MailboxParcel;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MailboxParcelEntry(UUID id, String sender, long itemCount) {
    private static final int MAX_ENTRIES = 4;

    public static MailboxParcelEntry from(MailboxParcel parcel) {
        return new MailboxParcelEntry(parcel.id(), parcel.sender(), parcel.itemCount());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(id.getMostSignificantBits());
        buffer.writeLong(id.getLeastSignificantBits());
        buffer.writeUtf(sender, 256);
        buffer.writeLong(itemCount);
    }

    public static MailboxParcelEntry read(FriendlyByteBuf buffer) {
        UUID id = new UUID(buffer.readLong(), buffer.readLong());
        String sender = buffer.readUtf(256);
        long itemCount = buffer.readLong();

        return new MailboxParcelEntry(id, sender, itemCount);
    }

    public static void writeList(FriendlyByteBuf buffer, List<MailboxParcelEntry> entries) {
        if (entries.size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("Mailbox snapshot contains too many entries: " + entries.size());
        }

        buffer.writeVarInt(entries.size());

        for (MailboxParcelEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static List<MailboxParcelEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalStateException("Invalid mailbox entry count: " + size);
        }

        List<MailboxParcelEntry> entries = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            entries.add(read(buffer));
        }
        return List.copyOf(entries);
    }
}
