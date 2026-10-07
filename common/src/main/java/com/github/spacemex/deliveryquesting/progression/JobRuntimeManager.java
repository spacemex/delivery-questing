package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.job.JobDefinition;

import java.util.Optional;

public final class JobRuntimeManager {

    private JobRuntimeManager() {}

    public static Optional<String> getAcceptanceFailure(DeliveryGroup group, JobDefinition job) {
        if (group.hasActiveJobDefinition(job.id())) {
            return Optional.of("Job is already active.");
        }

        if (group.level() < job.minLevel()) {
            return Optional.of("Job requires level " + job.minLevel()
                    + ". Your group is level " + group.wholeLevel() + ".");
        }

        return Optional.empty();
    }

    public static ActionResult acceptJob(DeliveryQuestingSavedData data, DeliveryGroup group, JobDefinition job) {
        Optional<String> failure = getAcceptanceFailure(group, job);

        if (failure.isPresent()) {
            return new ActionResult(false, failure.get());
        }

        Optional<java.util.UUID> instanceId = data.acceptJob(group.id(), job.id());

        if (instanceId.isEmpty()) {
            return new ActionResult(false, "Failed to activate job.");
        }

        return new ActionResult(true, "Accepted repeatable job '" + job.name() + "'.");
    }

    public record ActionResult(boolean success, String message) {}
}