package com.github.spacemex.deliveryquesting.task;

import java.util.Objects;

public record ContractorDefinition(String name, String profession, String skin) {

    public ContractorDefinition {
        Objects.requireNonNull(name, "name");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Contractor name cannot be blank");
        }

        profession = profession == null ? "" : profession;
        skin = skin == null ? "" : skin;
    }
}
