package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.task.ItemRequirement;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

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
}
