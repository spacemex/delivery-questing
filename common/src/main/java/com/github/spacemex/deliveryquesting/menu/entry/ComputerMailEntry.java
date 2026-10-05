package com.github.spacemex.deliveryquesting.menu.entry;

import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.GroupEmail;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ComputerMailEntry(UUID emailId, boolean read, boolean canAccept, BulletinBoardTaskEntry task) {
    private static final int MAX_EMAILS = 1024;

    public static ComputerMailEntry from(GroupEmail email, DeliveryGroup group, TaskDefinition task) {
        return new ComputerMailEntry(email.id(), email.read(), TaskRuntimeManager.getAcceptanceFailure(group, task).isEmpty(),
                BulletinBoardTaskEntry.fromAvailable(task));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(emailId.getMostSignificantBits());
        buffer.writeLong(emailId.getLeastSignificantBits());
        buffer.writeBoolean(read);
        buffer.writeBoolean(canAccept);
        task.write(buffer);
    }

    public static ComputerMailEntry read(FriendlyByteBuf buffer) {
        UUID emailId = new UUID(buffer.readLong(), buffer.readLong());
        boolean read = buffer.readBoolean();
        boolean canAccept = buffer.readBoolean();
        BulletinBoardTaskEntry task = BulletinBoardTaskEntry.read(buffer);

        return new ComputerMailEntry(emailId, read, canAccept, task);
    }

    public static void writeList(FriendlyByteBuf buffer, List<ComputerMailEntry> entries) {
        if (entries.size() > MAX_EMAILS) {
            throw new IllegalArgumentException("Too many Computer emails: " + entries.size());
        }

        buffer.writeVarInt(entries.size());

        for (ComputerMailEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static List<ComputerMailEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_EMAILS) {

            throw new IllegalStateException("Invalid Computer email count: " + size);
        }

        List<ComputerMailEntry> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(read(buffer));
        }

        return List.copyOf(result);
    }
}