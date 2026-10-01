package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.progression.GroupEmail;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ComputerInboxEntry(UUID emailId, GroupEmail.Type type, boolean read, Identifier referenceId, String title,
                                 String sender) {
    private static final int MAX_EMAILS = 1024;

    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(emailId.getMostSignificantBits());
        buffer.writeLong(emailId.getLeastSignificantBits());
        buffer.writeUtf(type.name(), 32);
        buffer.writeBoolean(read);
        buffer.writeUtf(referenceId.toString(), 256);
        buffer.writeUtf(title, 512);
        buffer.writeUtf(sender, 256);
    }

    public static ComputerInboxEntry read(FriendlyByteBuf buffer) {
        UUID emailId = new UUID(buffer.readLong(), buffer.readLong());
        GroupEmail.Type type = GroupEmail.Type.valueOf(buffer.readUtf(32));
        boolean read = buffer.readBoolean();
        Identifier referenceId = Identifier.parse(buffer.readUtf(256));
        String title = buffer.readUtf(512);
        String sender = buffer.readUtf(256);

        return new ComputerInboxEntry(emailId, type, read, referenceId, title, sender);
    }

    public static void writeList(FriendlyByteBuf buffer, List<ComputerInboxEntry> entries) {
        if (entries.size() > MAX_EMAILS) {
            throw new IllegalArgumentException("Too many inbox entries: " + entries.size());
        }

        buffer.writeVarInt(entries.size());

        for (ComputerInboxEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static List<ComputerInboxEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_EMAILS) {
            throw new IllegalStateException("Invalid Computer inbox size: " + size);
        }

        List<ComputerInboxEntry> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(read(buffer));
        }

        return List.copyOf(result);
    }
}