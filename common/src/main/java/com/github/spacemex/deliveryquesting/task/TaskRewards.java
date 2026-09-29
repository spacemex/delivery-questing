package com.github.spacemex.deliveryquesting.task;

import java.util.List;
import java.util.Objects;

public record TaskRewards(int experience, long money, List<ItemReward> items) {

    public TaskRewards {
        if (experience < 0) {
            throw new IllegalArgumentException("Task reward experience cannot be negative");
        }

        if (money < 0L) {
            throw new IllegalArgumentException("Task reward money cannot be negative");
        }

        Objects.requireNonNull(items, "items");
        items = List.copyOf(items);
    }

    public static TaskRewards empty() {
        return new TaskRewards(0, 0L, List.of());
    }
}
