package com.github.spacemex.deliveryquesting.task.entry;

import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import net.minecraft.resources.Identifier;

import java.util.Locale;
import java.util.Objects;

public record FluidRequirement(TargetType targetType, Identifier target, long amount) implements TaskRequirement {

    public FluidRequirement {
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(target, "target");

        if (amount <= 0L) {
            throw new IllegalArgumentException("Fluid requirement amount must be greater than 0");
        }
    }

    public static FluidRequirement fluid(Identifier fluid, long amount) {
        return new FluidRequirement(TargetType.FLUID, fluid, amount);
    }

    public static FluidRequirement tag(Identifier tag, long amount) {
        return new FluidRequirement(TargetType.TAG, tag, amount);
    }

    @Override
    public String progressKey() {
        return "fluid/" + targetType.name().toLowerCase(Locale.ROOT) + "/" + target;
    }

    public enum TargetType {
        FLUID,
        TAG
    }
}