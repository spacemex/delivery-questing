package com.github.spacemex.deliveryquesting.task.definition;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record OfferDefinition(Identifier id, Identifier item, int count, long price, int minLevel,
                              boolean forEveryMember) {

    public OfferDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(item, "item");

        if (count <= 0) {
            throw new IllegalArgumentException("Offer item count must be greater than 0");
        }

        if (price <= 0L) {
            throw new IllegalArgumentException("Offer price must be greater than 0");
        }

        if (minLevel < 0) {
            throw new IllegalArgumentException("Offer minimum level cannot be negative");
        }
    }
}