package com.github.spacemex.deliveryquesting.command;

import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskProgress;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class TaskCommand {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("delivery").then(Commands.literal("task")
                        .then(Commands.literal("available").requires(CommandSourceStack::isPlayer)
                                .executes(TaskCommand::showAvailableTasks))
                        .then(Commands.literal("accept").requires(CommandSourceStack::isPlayer)
                                .then(Commands.argument("task", IdentifierArgument.id())
                                        .suggests((context, builder) -> suggestTasks(builder)).executes(TaskCommand::acceptTask)))
                        .then(Commands.literal("info").requires(CommandSourceStack::isPlayer)
                                .then(Commands.argument("task", IdentifierArgument.id())
                                        .suggests((context, builder) -> suggestTasks(builder)).executes(TaskCommand::showTaskInfo)))
                        .then(Commands.literal("submit").requires(CommandSourceStack::isPlayer)
                                .then(Commands.argument("task", IdentifierArgument.id())
                                        .suggests((context, builder) -> suggestTasks(builder)).executes(TaskCommand::submitTask))))
        );
    }

    private static CompletableFuture<Suggestions> suggestTasks(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(TaskManager.getTasks().stream()
                .map(task -> task.id().toString()), builder);
    }

    private static int showAvailableTasks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        var available = TaskManager.getTasks().stream().filter(task -> TaskRuntimeManager.getAcceptanceFailure(group, task).isEmpty()).toList();

        if (available.isEmpty()) {
            source.sendFailure(Component.literal("There are no tasks currently available."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("----- Available Tasks -----"), false);

        for (TaskDefinition task : available) {
            source.sendSuccess(() -> Component.literal(task.id() + " - " + task.name()), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int acceptTask(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Identifier taskId = IdentifierArgument.getId(context, "task");
        Optional<TaskDefinition> optionalTask = TaskManager.getTask(taskId);

        if (optionalTask.isEmpty()) {
            source.sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        TaskRuntimeManager.ActionResult result = TaskRuntimeManager.acceptTask(data, optionalGroup.get(), optionalTask.get());

        if (!result.success()) {
            source.sendFailure(Component.literal(result.message()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(result.message()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int showTaskInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Identifier taskId = IdentifierArgument.getId(context, "task");
        Optional<TaskDefinition> optionalTask = TaskManager.getTask(taskId);

        if (optionalTask.isEmpty()) {
            source.sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();
        TaskDefinition task = optionalTask.get();

        source.sendSuccess(() -> Component.literal("----- " + task.name() + " -----"), false);
        source.sendSuccess(() -> Component.literal("ID: " + task.id()), false);
        source.sendSuccess(() -> Component.literal(task.description()), false);
        source.sendSuccess(() -> Component.literal("Contractor: " + task.contractor().name()), false);
        source.sendSuccess(() -> Component.literal("Required Level: " + task.minLevel()), false);

        String status;

        if (group.hasCompletedTask(task.id())) {
            status = "Completed";
        } else if (group.hasActiveTask(task.id())) {
            status = "Active";
        } else {
            Optional<String> failure = TaskRuntimeManager.getAcceptanceFailure(group, task);
            status = failure.isEmpty() ? "Available" : "Locked";
        }

        source.sendSuccess(() -> Component.literal("Status: " + status), false);

        Optional<TaskProgress> progress = group.getActiveTask(task.id());

        if (progress.isPresent()) {
            source.sendSuccess(() -> Component.literal("Requirements:"), false);

            for (TaskRequirement requirement : task.requirements()) {
                long current = progress.get().getProgress(requirement);

                source.sendSuccess(() -> Component.literal("  " + requirement.progressKey()
                        + ": " + current + " / " + requirement.amount()), false);
            }
        }

        source.sendSuccess(() -> Component.literal("Rewards: " + task.rewards().experience() + " XP, "
                + task.rewards().money() + " money"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int submitTask(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Identifier taskId = IdentifierArgument.getId(context, "task");
        Optional<TaskDefinition> optionalTask = TaskManager.getTask(taskId);

        if (optionalTask.isEmpty()) {
            source.sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        TaskDefinition task = optionalTask.get();
        TaskRuntimeManager.SubmissionResult result = TaskRuntimeManager.submitItems(player, data, optionalGroup.get(), task);

        if (!result.success()) {
            source.sendFailure(Component.literal(result.message()));

            return 0;
        }

        source.sendSuccess(() -> Component.literal(result.message()), false);

        if (result.completed()) {
            source.sendSuccess(() -> Component.literal("Rewards: +" + task.rewards().experience() + " XP, +"
                    + task.rewards().money() + " money"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
