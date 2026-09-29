package com.github.spacemex.deliveryquesting.task;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ItemRequirement(TargetType targetType, Identifier target, long amount) implements TaskRequirement {

    public ItemRequirement {
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(target, "target");

        if (amount <= 0L) {
            throw new IllegalArgumentException("Item requirement amount must be greater than 0");
        }
    }

    public static ItemRequirement item(Identifier item, long amount) {
        return new ItemRequirement(TargetType.ITEM, item, amount);
    }

    public static ItemRequirement tag(Identifier tag, long amount) {
        return new ItemRequirement(TargetType.TAG, tag, amount);
    }

    public boolean isItem() {
        return targetType == TargetType.ITEM;
    }

    public enum TargetType {
        ITEM,
        TAG
    }
}
