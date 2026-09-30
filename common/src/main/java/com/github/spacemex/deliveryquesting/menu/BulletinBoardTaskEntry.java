package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BulletinBoardTaskEntry(Identifier id, String name, String description, String contractor, int minLevel,
                                     int experienceReward, long moneyReward) {
    private static final int MAX_TASKS = 1024;

    public BulletinBoardTaskEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(contractor, "contractor");
    }

    public static BulletinBoardTaskEntry from(TaskDefinition task) {
        return new BulletinBoardTaskEntry(task.id(), task.name(), task.description(), task.contractor().name(), task.minLevel(), task.rewards().experience(), task.rewards().money());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(id.toString(), 256);
        buffer.writeUtf(name, 256);
        buffer.writeUtf(description, 256);
        buffer.writeUtf(contractor, 256);
        buffer.writeVarInt(minLevel);
        buffer.writeVarInt(experienceReward);
        buffer.writeLong(moneyReward);
    }

    public static BulletinBoardTaskEntry read(FriendlyByteBuf buffer) {
        Identifier id = Identifier.parse(buffer.readUtf(256));
        String name = buffer.readUtf(256);
        String description = buffer.readUtf(256);
        String contractor = buffer.readUtf(256);
        int minLevel = buffer.readVarInt();
        int experienceReward = buffer.readVarInt();
        long moneyReward = buffer.readLong();

        return new BulletinBoardTaskEntry(id, name, description, contractor, minLevel, experienceReward, moneyReward);
    }

    public static void writeList(FriendlyByteBuf buffer, List<BulletinBoardTaskEntry> tasks) {
        if (tasks.size() > MAX_TASKS) {
            throw new IllegalArgumentException("Too many bulletin board tasks: " + tasks.size());
        }

        buffer.writeVarInt(tasks.size());

        for (BulletinBoardTaskEntry task : tasks) {
            task.write(buffer);
        }
    }

    public static List<BulletinBoardTaskEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_TASKS) {
            throw new IllegalStateException("Invalid bulletin board task count: " + size);
        }

        List<BulletinBoardTaskEntry> tasks = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            tasks.add(read(buffer));
        }
        return List.copyOf(tasks);
    }
}
