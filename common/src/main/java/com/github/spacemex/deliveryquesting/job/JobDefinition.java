package com.github.spacemex.deliveryquesting.job;

import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import com.github.spacemex.deliveryquesting.task.definition.ContractorDefinition;
import com.github.spacemex.deliveryquesting.task.entry.TaskRewards;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record JobDefinition(Identifier id, String name, String description, ContractorDefinition contractor,
                            int minLevel, List<TaskRequirement> requirements, TaskRewards rewards) {

    public JobDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(contractor, "contractor");
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(rewards, "rewards");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Job name cannot be blank");
        }

        if (minLevel < 0) {
            throw new IllegalArgumentException("Job minimum level cannot be negative");
        }

        requirements = List.copyOf(requirements);

        validateRequirementKeys(id, requirements);
    }

    private static void validateRequirementKeys(Identifier jobId, List<TaskRequirement> requirements) {
        Set<String> keys = new HashSet<>();

        for (TaskRequirement requirement : requirements) {
            if (!keys.add(requirement.progressKey())) {
                throw new IllegalArgumentException("Job '" + jobId + "' contains duplicate requirement '"
                        + requirement.progressKey() + "'");
            }
        }
    }
}