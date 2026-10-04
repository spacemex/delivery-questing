package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.job.JobDefinition;
import com.github.spacemex.deliveryquesting.job.JobManager;
import com.github.spacemex.deliveryquesting.task.ItemRequirement;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TaskRuntimeManager {

    public static Optional<String> getAcceptanceFailure(DeliveryGroup group, TaskDefinition task) {
        if (group.hasCompletedTask(task.id())) {
            return Optional.of("Task has already been completed.");
        }

        if (group.hasActiveTask(task.id())) {
            return Optional.of("Task is already active.");
        }

        if (group.level() < task.minLevel()) {
            return Optional.of("Task requires level " + task.minLevel() + ". Your group is level " + group.wholeLevel() + ".");
        }

        for (var dependency : task.dependencies()) {
            if (!group.hasCompletedTask(dependency)) {
                return Optional.of("Missing required task: " + dependency);
            }
        }

        return Optional.empty();
    }

    public static MailboxSubmissionResult submitDeliveryItems(DeliveryQuestingSavedData data, DeliveryGroup group, List<ItemStack> outgoing) {
        return submitMailboxItems(data, group, outgoing);
    }

    public static ActionResult acceptTask(DeliveryQuestingSavedData data, DeliveryGroup group, TaskDefinition task) {
        Optional<String> failure = getAcceptanceFailure(group, task);

        if (failure.isPresent()) {
            return new ActionResult(false, failure.get());
        }

        if (!data.acceptTask(group.id(), task.id())) {
            return new ActionResult(false, "Failed to activate task.");
        }

        return new ActionResult(true, "Accepted task '" + task.name() + "'.");
    }

    public static SubmissionResult submitItems(ServerPlayer player, DeliveryQuestingSavedData data, DeliveryGroup group, TaskDefinition task) {
        Optional<TaskProgress> optionalProgress = group.getActiveTask(task.id());

        if (optionalProgress.isEmpty()) {
            return new SubmissionResult(false, 0L, false, "Task is not active.");
        }

        TaskProgress progress = optionalProgress.get();
        long submitted = 0L;

        for (TaskRequirement requirement : task.requirements()) {
            if (!(requirement instanceof ItemRequirement itemRequirement)) {
                continue; //Todo: Future Fluid and Other Misc Requirements
            }

            long remaining = progress.getRemaining(requirement);

            if (remaining <= 0L) {
                continue;
            }

            long consumed = consumeItems(player, itemRequirement, remaining);

            if (consumed <= 0L) {
                continue;
            }

            long accepted = data.addTaskProgress(group.id(), task.id(), requirement, consumed);
            submitted += accepted;
        }

        if (progress.isComplete(task)) {
            if (!data.completeTask(group.id(), task)) {
                return new SubmissionResult(false, submitted, false, "Task progress is complete, but the task could not be finalized.");
            }

            return new SubmissionResult(true, submitted, true, "Completed task '" + task.name() + "'.");
        }

        if (submitted <= 0L) {
            return new SubmissionResult(false, 0L, false, "You do not have any matching items to submit.");
        }

        return new SubmissionResult(true, submitted, false, "Submitted " + submitted + " item(s).");
    }

    private static long consumeItems(ServerPlayer player, ItemRequirement requirement, long requested) {
        long remaining = requested;
        long consumed = 0L;

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (remaining <= 0L) {
                break;
            }

            if (stack.isEmpty()) {
                continue;
            }

            if (!matches(stack, requirement)) {
                continue;
            }

            int take = (int) Math.min(remaining, stack.getCount());

            stack.shrink(take);

            consumed += take;
            remaining -= take;
        }

        if (consumed > 0L) {
            player.getInventory().setChanged();
        }

        return consumed;
    }

    public static MailboxSubmissionResult submitMailboxItems(DeliveryQuestingSavedData data, DeliveryGroup group, List<ItemStack> outgoing) {
        long submitted = 0L;
        long totalItems = 0L;

        for (ItemStack original : outgoing) {
            if (original.isEmpty()) {
                continue;
            }

            ItemStack stack = original.copy();

            totalItems += stack.getCount();

            while (!stack.isEmpty()) {
                boolean inserted = false;
                List<TaskProgress> activeTasks = new ArrayList<>(group.activeTasks());

                taskLoop:
                for (TaskProgress progress : activeTasks) {
                    Optional<TaskDefinition> optionalTask = TaskManager.getTask(progress.taskId());

                    if (optionalTask.isEmpty()) {
                        continue;
                    }

                    TaskDefinition task = optionalTask.get();

                    for (TaskRequirement requirement : task.requirements()) {
                        if (!(requirement instanceof ItemRequirement itemRequirement)) {
                            continue;
                        }

                        long remaining = progress.getRemaining(requirement);

                        if (remaining <= 0L || !matches(stack, itemRequirement)) {
                            continue;
                        }

                        int amount = (int) Math.min(remaining, stack.getCount());
                        long accepted = data.addTaskProgress(group.id(), task.id(), requirement, amount);

                        if (accepted <= 0L) {
                            continue;
                        }

                        stack.shrink((int) accepted);
                        submitted += accepted;
                        inserted = true;

                        break taskLoop;
                    }
                }

                if (!inserted) {
                    List<DeliveryJobProgress> activeJobs = new ArrayList<>(group.activeJobs());

                    jobLoop:
                    for (DeliveryJobProgress progress : activeJobs) {

                        Optional<JobDefinition> optionalJob = JobManager.getJob(progress.jobId());

                        if (optionalJob.isEmpty()) {
                            continue;
                        }

                        JobDefinition job = optionalJob.get();

                        for (TaskRequirement requirement : job.requirements()) {

                            if (!(requirement instanceof ItemRequirement itemRequirement)) {
                                continue;
                            }

                            long remaining = progress.getRemaining(requirement);

                            if (remaining <= 0L || !matches(stack, itemRequirement)) {

                                continue;
                            }

                            int amount = (int) Math.min(remaining, stack.getCount());
                            long accepted = data.addJobProgress(group.id(), progress.instanceId(), requirement, amount);

                            if (accepted <= 0L) {
                                continue;
                            }

                            stack.shrink((int) accepted);
                            submitted += accepted;
                            inserted = true;

                            break jobLoop;
                        }
                    }
                }

                if (!inserted) {
                    break;
                }
            }
        }

        List<Identifier> completedTasks = new ArrayList<>();

        for (TaskProgress progress : new ArrayList<>(group.activeTasks())) {

            Optional<TaskDefinition> optionalTask = TaskManager.getTask(progress.taskId());

            if (optionalTask.isEmpty()) {
                continue;
            }

            TaskDefinition task = optionalTask.get();

            if (!progress.isComplete(task)) {
                continue;
            }

            if (data.completeTask(group.id(), task)) {
                completedTasks.add(task.id());
            }
        }

        List<UUID> completedJobs = new ArrayList<>();

        for (DeliveryJobProgress progress : new ArrayList<>(group.activeJobs())) {

            Optional<JobDefinition> optionalJob = JobManager.getJob(progress.jobId());

            if (optionalJob.isEmpty()) {
                continue;
            }

            JobDefinition job = optionalJob.get();

            if (!progress.isComplete(job)) {
                continue;
            }

            if (data.completeJob(group.id(), progress.instanceId(), job)) {
                completedJobs.add(progress.instanceId());
            }
        }

        return new MailboxSubmissionResult(submitted, Math.max(0L, totalItems - submitted), List.copyOf(completedTasks), List.copyOf(completedJobs));
    }

    @SuppressWarnings("deprecation")
    private static boolean matches(ItemStack stack, ItemRequirement requirement) {
        return switch (requirement.targetType()) {
            case ITEM -> requirement.target().equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            case TAG -> {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, requirement.target());
                yield stack.getItem().builtInRegistryHolder().is(tag);
            }
        };
    }

    public record ActionResult(boolean success, String message) {
    }

    public record SubmissionResult(boolean success, long submitted, boolean completed, String message) {
    }

    public record MailboxSubmissionResult(long submitted, long discarded, List<Identifier> completedTasks,
                                          List<UUID> completedJobs) {
    }
}
