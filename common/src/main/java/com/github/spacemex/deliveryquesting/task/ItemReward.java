package com.github.spacemex.deliveryquesting.task;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ItemReward(Identifier item, int count) {

    public ItemReward {
        Objects.requireNonNull(item, "item");

        if (count <= 0) {
            throw new IllegalArgumentException("Item reward count must be greater than 0");
        }
    }
}
