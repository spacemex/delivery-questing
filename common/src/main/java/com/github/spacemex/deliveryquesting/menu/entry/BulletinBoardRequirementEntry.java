package com.github.spacemex.deliveryquesting.menu.entry;

import com.github.spacemex.deliveryquesting.task.ItemRequirement;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BulletinBoardRequirementEntry(ItemRequirement.TargetType targetType, Identifier target, long current,
                                            long required) {
    private static final int MAX_REQUIREMENTS = 128;

    public BulletinBoardRequirementEntry {
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(target, "target");

        if (current < 0L) {
            throw new IllegalArgumentException("Requirement progress cannot be negative");
        }

        if (required <= 0L) {
            throw new IllegalArgumentException("Requirement amount must be greater than 0");
        }
    }

    public static BulletinBoardRequirementEntry from(TaskRequirement requirement, long current) {
        if (!(requirement instanceof ItemRequirement itemRequirement)) {
            throw new IllegalArgumentException("Unsupported requirement type: " + requirement.getClass().getName());
        }

        return new BulletinBoardRequirementEntry(itemRequirement.targetType(), itemRequirement.target(), current, itemRequirement.amount());
    }

    public boolean complete() {
        return current >= required;
    }

    public boolean isItem() {
        return targetType == ItemRequirement.TargetType.ITEM;
    }

    public boolean isTag() {
        return targetType == ItemRequirement.TargetType.TAG;
    }

    public String label() {
        return isTag() ? "#" + target : target.toString();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(targetType);
        buffer.writeUtf(target.toString(), 256);
        buffer.writeLong(current);
        buffer.writeLong(required);
    }

    public static BulletinBoardRequirementEntry read(FriendlyByteBuf buffer) {
        ItemRequirement.TargetType targetType = buffer.readEnum(ItemRequirement.TargetType.class);

        Identifier target = Identifier.parse(buffer.readUtf(256));
        long current = buffer.readLong();
        long required = buffer.readLong();

        return new BulletinBoardRequirementEntry(targetType, target, current, required);
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