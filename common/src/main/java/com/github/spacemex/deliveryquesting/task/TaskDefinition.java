package com.github.spacemex.deliveryquesting.task;

import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record TaskDefinition(Identifier id, String name, String description, ContractorDefinition contractor,
                             int minLevel, List<Identifier> dependencies, boolean forced,
                             List<TaskRequirement> requirements, TaskRewards rewards) {

    public TaskDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(contractor, "contractor");
        Objects.requireNonNull(dependencies, "dependencies");
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(rewards, "rewards");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be blank");
        }

        if (minLevel < 0) {
            throw new IllegalArgumentException("Task minimum level cannot be negative");
        }

        dependencies = List.copyOf(dependencies);
        requirements = List.copyOf(requirements);

        validateRequirementKeys(id, requirements);
    }

    private static void validateRequirementKeys(Identifier taskId, List<TaskRequirement> requirements) {
        Set<String> keys = new HashSet<>();

        for (TaskRequirement requirement : requirements) {
            if (!keys.add(requirement.progressKey())) {
                throw new IllegalArgumentException("Task '" + taskId + "' contains duplicate requirement '"
                        + requirement.progressKey() + "'");
            }
        }
    }
}
