package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.task.ItemRequirement;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BulletinBoardRequirementEntry(String label, long current, long required) {
    private static final int MAX_REQUIREMENTS = 128;

    public BulletinBoardRequirementEntry {
        Objects.requireNonNull(label, "label");

        if (label.isBlank()) {
            throw new IllegalArgumentException("Requirement label cannot be blank");
        }

        if (current < 0L) {
            throw new IllegalArgumentException("Requirement progress cannot be negative");
        }

        if (required < 0L) {
            throw new IllegalArgumentException("Requirement amount must be greater than 0");
        }
    }

    public static BulletinBoardRequirementEntry from(TaskRequirement requirement, long current) {
        String label;

        if (requirement instanceof ItemRequirement itemRequirement) {
            label = switch (itemRequirement.targetType()) {
                case ITEM -> itemRequirement.target().toString();
                case TAG -> "#" + itemRequirement.target().toString();
            };
        } else {
            label = requirement.progressKey();
        }

        return new BulletinBoardRequirementEntry(label, current, requirement.amount());
    }

    public boolean complete() {
        return current >= required;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(label, 512);
        buffer.writeLong(current);
        buffer.writeLong(required);
    }

    public static BulletinBoardRequirementEntry read(FriendlyByteBuf buffer) {
        return new BulletinBoardRequirementEntry(buffer.readUtf(512), buffer.readLong(), buffer.readLong());
    }

    public static void writeList(FriendlyByteBuf buffer, List<BulletinBoardRequirementEntry> requirements) {
        if (requirements.size() > MAX_REQUIREMENTS) {
            throw new IllegalArgumentException("Too many requirements: " + requirements.size());
        }

        buffer.writeVarInt(requirements.size());

        for (BulletinBoardRequirementEntry requirement : requirements) {
            requirement.write(buffer);
        }
    }

    public static List<BulletinBoardRequirementEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_REQUIREMENTS) {
            throw new IllegalStateException("Invalid task requirement count: " + size);
        }

        List<BulletinBoardRequirementEntry> result = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            result.add(read(buffer));
        }

        return List.copyOf(result);
    }
}
