package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.job.JobDefinition;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class DeliveryJobProgress {
    private static final Codec<Map<String, Long>> PROGRESS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);
    public static final Codec<DeliveryJobProgress> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("instance").forGetter(DeliveryJobProgress::instanceId),
                    Identifier.CODEC.fieldOf("job").forGetter(DeliveryJobProgress::jobId),
                    PROGRESS_CODEC.optionalFieldOf("progress", Map.of()).forGetter(DeliveryJobProgress::progress)
            ).apply(instance, DeliveryJobProgress::new));
    private final UUID instanceId;
    private final Identifier jobId;
    private final Map<String, Long> progress;

    public DeliveryJobProgress(Identifier jobId) {
        this(UUID.randomUUID(), jobId, Map.of());
    }

    private DeliveryJobProgress(UUID instanceId, Identifier jobId, Map<String, Long> progress) {
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
        this.jobId = Objects.requireNonNull(jobId, "jobId");

        Objects.requireNonNull(progress, "progress");

        this.progress = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : progress.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("Job progress key cannot be blank");
            }

            if (entry.getValue() == null || entry.getValue() < 0L) {
                throw new IllegalArgumentException("Job progress cannot be negative");
            }

            this.progress.put(entry.getKey(), entry.getValue());
        }
    }

    public UUID instanceId() {
        return instanceId;
    }

    public Identifier jobId() {
        return jobId;
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

    public boolean isComplete(JobDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        if (!jobId.equals(definition.id())) {
            throw new IllegalArgumentException("Job progress for '" + jobId + "' cannot be checked against job '" + definition.id() + "'");
        }

        for (TaskRequirement requirement : definition.requirements()) {
            if (getProgress(requirement) < requirement.amount()) {
                return false;
            }
        }

        return true;
    }
}