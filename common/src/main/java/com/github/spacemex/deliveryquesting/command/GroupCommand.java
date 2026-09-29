package com.github.spacemex.deliveryquesting.command;

import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Optional;

/**
 * TODO:
 * Add Command Suggestions,
 * Add Context Suggestions,
 * <p>
 * Add More Commands,
 * Remove Debug Commands
 */
public final class GroupCommand {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> register(dispatcher));
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("delivery")
                        .then(Commands.literal("group")
                                .then(Commands.literal("create").requires(CommandSourceStack::isPlayer)
                                        .then(Commands.argument("name", StringArgumentType.greedyString()).executes(GroupCommand::createGroup)))
                                .then(Commands.literal("info").requires(CommandSourceStack::isPlayer).executes(GroupCommand::showGroupInfo)))

                        .then(Commands.literal("debug").then(Commands.literal("addxp").requires(GroupCommand::canUseDebugCommands)
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1L)).executes(GroupCommand::addExperience)))
                                .then(Commands.literal("addmoney").requires(GroupCommand::canUseDebugCommands)
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1L)).executes(GroupCommand::addMoney))))
        );
    }

    private static int createGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String name = StringArgumentType.getString(context, "name").trim();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());

        try {
            DeliveryGroup group = data.createGroup(name, player.getUUID());

            source.sendSuccess(() -> Component.literal("Created delivery group '" + group.name() + "'."), false);
            source.sendSuccess(() -> Component.literal("Group ID: " + group.id()), false);

            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException | IllegalStateException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int showGroupInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();
        source.sendSuccess(() -> Component.literal("----- Delivery Group -----"), false);
        source.sendSuccess(() -> Component.literal("Name: " + group.name()), false);
        source.sendSuccess(() -> Component.literal("ID: " + group.id()), false);
        source.sendSuccess(() -> Component.literal("Members: " + group.members().size()), false);
        source.sendSuccess(() -> Component.literal("Experience: " + group.experience()), false);
        source.sendSuccess(() -> Component.literal("Balance: " + group.balance()), false);
        source.sendSuccess(() -> Component.literal("Active Tasks: " + group.activeTasks().size()), false);
        source.sendSuccess(() -> Component.literal("Completed Tasks: " + group.completedTasks().size()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int addExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        long amount = LongArgumentType.getLong(context, "amount");
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!data.addExperience(group.id(), amount)) {
            source.sendFailure(Component.literal("Failed to add group experience."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Added " + amount + " experience to '" + group.name() + "'. New total: " + group.experience()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        long amount = LongArgumentType.getLong(context, "amount");
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!data.addBalance(group.id(), amount)) {
            source.sendFailure(Component.literal("Failed to add group money."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Added " + amount + " money to '" + group.name() + "'. New balance: " + group.balance()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static boolean canUseDebugCommands(CommandSourceStack source) {
        return source.isPlayer() && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }
}
