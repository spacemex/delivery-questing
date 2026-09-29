package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TaskProgress {
    private static final Codec<Map<String, Long>> PROGRESS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);
    public static final Codec<TaskProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("task").forGetter(TaskProgress::taskId),
            PROGRESS_CODEC.optionalFieldOf("progress", Map.of()).forGetter(TaskProgress::progress)
    ).apply(instance, TaskProgress::new));
    private final Identifier taskId;
    private final Map<String, Long> progress;

    public TaskProgress(Identifier taskId) {
        this(taskId, Map.of());
    }

    private TaskProgress(Identifier taskId, Map<String, Long> progress) {
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(progress, "progress");
        this.progress = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : progress.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Key cannot be null or blank");
            }

            if (value == null || value < 0L) {
                throw new IllegalArgumentException("Task progress cannot be negative");
            }

            this.progress.put(key, value);
        }
    }

    public Identifier taskId() {
        return taskId;
    }

    public Map<String, Long> progress() {
        return Collections.unmodifiableMap(progress);
    }

    public long getProgress(TaskRequirement requirement) {
        return progress.getOrDefault(requirement.progressKey(), 0L);
    }

    public long getRemaining(TaskRequirement requirement) {
        return Math.max(0L, requirement.amount() - getProgress(requirement));
    }

    long addProgress(TaskRequirement requirement, long amount) {
        if (amount <= 0L) {
            return 0L;
        }

        long current = getProgress(requirement);
        long remaining = Math.max(0L, requirement.amount() - current);

        if (remaining <= 0L) {
            return 0L;
        }

        long accepted = Math.min(remaining, amount);

        progress.put(requirement.progressKey(), current + accepted);
        return accepted;
    }

    public boolean isComplete(TaskDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        if (!taskId.equals(definition.id())) {
            throw new IllegalArgumentException("TaskProgress for '" + taskId + "' cannot be checked against task '"
                    + definition.id() + "'");
        }

        for (TaskRequirement requirement : definition.requirements()) {
            if (getProgress(requirement) < requirement.amount()) {
                return false;
            }
        }
        return true;
    }
}
