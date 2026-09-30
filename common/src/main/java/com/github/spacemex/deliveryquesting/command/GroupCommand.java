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

public final class GroupCommand {
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
                Commands.literal("delivery")

                        .then(
                                Commands.literal("group")

                                        .then(
                                                Commands.literal("create")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "name",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::createGroup
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("info")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .executes(
                                                                GroupCommand::showGroupInfo
                                                        )
                                        )

                                        .then(
                                                Commands.literal("invite")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "player",
                                                                                StringArgumentType.word()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::inviteMember
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("invitations")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .executes(
                                                                GroupCommand::showInvitations
                                                        )
                                        )

                                        .then(
                                                Commands.literal("accept")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "group",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::acceptInvitation
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("decline")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "group",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::declineInvitation
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("remove")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "player",
                                                                                StringArgumentType.word()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::removeMember
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("leave")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .executes(
                                                                GroupCommand::leaveGroup
                                                        )
                                        )

                                        .then(
                                                Commands.literal("transfer")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.argument(
                                                                                "player",
                                                                                StringArgumentType.word()
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::transferOwnership
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("delete")
                                                        .requires(CommandSourceStack::isPlayer)
                                                        .then(
                                                                Commands.literal("confirm")
                                                                        .executes(
                                                                                GroupCommand::deleteGroup
                                                                        )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("debug")

                                        .then(
                                                Commands.literal("addxp")
                                                        .requires(GroupCommand::canUseDebugCommands)
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1L)
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::addExperience
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("addmoney")
                                                        .requires(GroupCommand::canUseDebugCommands)
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1L)
                                                                        )
                                                                        .executes(
                                                                                GroupCommand::addMoney
                                                                        )
                                                        )
                                        )
                        )
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
        } catch (IllegalArgumentException | IllegalStateException exception) {
            source.sendFailure(Component.literal(exception.getMessage()));
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
        source.sendSuccess(() -> Component.literal("Owner: " + group.owner()), false);
        source.sendSuccess(() -> Component.literal("Members: " + group.members().size()), false);
        source.sendSuccess(() -> Component.literal("Pending Invitations: " + group.pendingInvitations().size()), false);
        source.sendSuccess(() -> Component.literal("Experience: " + group.experience()), false);
        source.sendSuccess(() -> Component.literal("Level: " + group.wholeLevel() + " (" + group.level() + ")"), false);
        source.sendSuccess(() -> Component.literal("Balance: " + group.balance()), false);
        source.sendSuccess(() -> Component.literal("Active Tasks: " + group.activeTasks().size()), false);
        source.sendSuccess(() -> Component.literal("Completed Tasks: " + group.completedTasks().size()), false);
        source.sendSuccess(() -> Component.literal("Mailbox Inbox: " + group.mailboxInbox().size() + " / " + DeliveryGroup.MAILBOX_INBOX_SIZE), false);
        source.sendSuccess(() -> Component.literal("Pending Mail: " + group.pendingMailbox().size()), false);
        source.sendSuccess(() -> Component.literal("Computer Unlocked: " + group.computerUnlocked()), false);

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

    private static int removeMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getPlayerOrException();

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("Only the group owner can remove members."));
            return 0;
        }

        String playerName = StringArgumentType.getString(context, "player");
        Optional<ServerPlayer> optionalTarget = findOnlinePlayer(source, playerName);

        if (optionalTarget.isEmpty()) {
            source.sendFailure(Component.literal("Player '" + playerName + "' is not online."));
            return 0;
        }

        ServerPlayer target = optionalTarget.get();

        if (target.getUUID().equals(player.getUUID())) {
            source.sendFailure(Component.literal("The group owner cannot remove themselves."));
            return 0;
        }

        if (!group.hasMember(target.getUUID())) {
            source.sendFailure(Component.literal(target.getName().getString() + " is not a member of your group."));
            return 0;
        }

        if (!data.removeMember(group.id(), target.getUUID())) {
            source.sendFailure(Component.literal("Failed to remove " + target.getName().getString() + "."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Removed " + target.getName().getString() + " from '" + group.name() + "'."), false);
        target.sendSystemMessage(Component.literal("You were removed from delivery group '" + group.name() + "'."));

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (group.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("The group owner cannot leave the group."));
            return 0;
        }

        if (!data.removeMember(group.id(), player.getUUID())) {
            source.sendFailure(Component.literal("Failed to leave the delivery group."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("You left delivery group '" + group.name() + "'."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int inviteMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("Only the group owner can invite members."));
            return 0;
        }

        String playerName = StringArgumentType.getString(context, "player");
        Optional<ServerPlayer> optionalTarget = findOnlinePlayer(source, playerName);

        if (optionalTarget.isEmpty()) {
            source.sendFailure(Component.literal("Player '" + playerName + "' is not online."));
            return 0;
        }

        ServerPlayer target = optionalTarget.get();

        if (target.getUUID().equals(player.getUUID())) {
            source.sendFailure(Component.literal("You cannot invite yourself."));
            return 0;
        }

        if (data.getGroupForPlayer(target.getUUID()).isPresent()) {
            source.sendFailure(Component.literal(target.getName().getString() + " is already in a delivery group."));
            return 0;
        }

        if (group.hasInvitation(target.getUUID())) {
            source.sendFailure(Component.literal(target.getName().getString() + " already has an invitation to your group."));
            return 0;
        }

        if (!data.invitePlayer(group.id(), target.getUUID())) {
            source.sendFailure(Component.literal("Failed to invite " + target.getName().getString() + "."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Invited " + target.getName().getString() + " to '" + group.name() + "'."), false);
        target.sendSystemMessage(Component.literal("You were invited to delivery group '" + group.name() + "'."));
        target.sendSystemMessage(Component.literal("Use /delivery group accept " + group.name() + " to join."));

        return Command.SINGLE_SUCCESS;
    }

    private static int showInvitations(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());

        var invitations = data.getInvitationsForPlayer(player.getUUID());

        if (invitations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You have no pending delivery group invitations."), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> Component.literal("----- Pending Invitations -----"), false);

        for (DeliveryGroup group : invitations) {
            source.sendSuccess(() -> Component.literal(group.name() + " [" + group.id() + "]"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int acceptInvitation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());

        if (data.getGroupForPlayer(player.getUUID()).isPresent()) {
            source.sendFailure(Component.literal("You are already in a delivery group."));
            return 0;
        }

        String groupName = StringArgumentType.getString(context, "group").trim();
        Optional<DeliveryGroup> optionalGroup = data.getGroupByName(groupName);

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("Unknown delivery group '" + groupName + "'."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.hasInvitation(player.getUUID())) {
            source.sendFailure(Component.literal("You do not have an invitation to '" + group.name() + "'."));
            return 0;
        }

        if (!data.acceptInvitation(group.id(), player.getUUID())) {
            source.sendFailure(Component.literal("Failed to join the delivery group."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Joined delivery group '" + group.name() + "'."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int declineInvitation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        String groupName = StringArgumentType.getString(context, "group").trim();
        Optional<DeliveryGroup> optionalGroup = data.getGroupByName(groupName);

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("Unknown delivery group '" + groupName + "'."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.hasInvitation(player.getUUID())) {
            source.sendFailure(Component.literal("You do not have an invitation to '" + group.name() + "'."));

            return 0;
        }

        if (!data.declineInvitation(group.id(), player.getUUID())) {
            source.sendFailure(Component.literal("Failed to decline the invitation."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Declined invitation to '" + group.name() + "'."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int transferOwnership(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("Only the group owner can transfer ownership."));
            return 0;
        }

        String playerName = StringArgumentType.getString(context, "player");
        Optional<ServerPlayer> optionalTarget = findOnlinePlayer(source, playerName);

        if (optionalTarget.isEmpty()) {
            source.sendFailure(Component.literal("Player '" + playerName + "' is not online."));
            return 0;
        }

        ServerPlayer target = optionalTarget.get();

        if (!group.hasMember(target.getUUID())) {
            source.sendFailure(Component.literal(target.getName().getString() + " is not a member of your group."));
            return 0;
        }

        if (group.isOwner(target.getUUID())) {
            source.sendFailure(Component.literal(target.getName().getString() + " is already the group owner."));
            return 0;
        }

        if (!data.transferOwnership(group.id(), player.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal("Failed to transfer group ownership."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Transferred ownership of '" + group.name() + "' to " + target.getName().getString() + "."), false);
        target.sendSystemMessage(Component.literal("You are now the owner of delivery group '" + group.name() + "'."));

        return Command.SINGLE_SUCCESS;
    }

    private static int deleteGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(source.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            source.sendFailure(Component.literal("You are not currently in a delivery group."));
            return 0;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("Only the group owner can delete the group."));
            return 0;
        }

        String groupName = group.name();

        if (!data.deleteGroup(group.id())) {
            source.sendFailure(Component.literal("Failed to delete the delivery group."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Deleted delivery group '" + groupName + "'."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static boolean canUseDebugCommands(CommandSourceStack source) {
        return source.isPlayer() && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static Optional<ServerPlayer> findOnlinePlayer(CommandSourceStack source, String name) {
        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.getName().getString().equalsIgnoreCase(name)).findFirst();
    }
}
