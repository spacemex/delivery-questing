package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptTaskPayload;
import com.github.spacemex.deliveryquesting.networking.packets.SubmitTaskPayload;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class TaskInteractionNetworkHandler {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        registerAcceptTask();
        registerSubmitTask();
    }

    private static void registerAcceptTask() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AcceptTaskPayload.TYPE, AcceptTaskPayload.CODEC, (payload, context) -> {
            if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            context.queue(() -> handleAcceptTask(serverPlayer, payload));
        });
    }

    private static void registerSubmitTask() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SubmitTaskPayload.TYPE, SubmitTaskPayload.CODEC, (payload, context) -> {
            if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            context.queue(() -> handleSubmitTask(serverPlayer, payload));
        });
    }

    private static void handleAcceptTask(ServerPlayer serverPlayer, AcceptTaskPayload payload) {
        if (!(serverPlayer.containerMenu instanceof BulletinBoardMenu menu)) {

            return;
        }

        if (!menu.hasAvailableTask(payload.taskId())) {
            return;
        }

        Optional<TaskDefinition> optionalTask = TaskManager.getTask(payload.taskId());

        if (optionalTask.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("That task no longer exists."));
            serverPlayer.closeContainer();
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(serverPlayer.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(serverPlayer.getUUID());

        if (optionalGroup.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("You are not currently in a delivery group."));
            serverPlayer.closeContainer();
            return;
        }

        TaskRuntimeManager.ActionResult result = TaskRuntimeManager.acceptTask(data, optionalGroup.get(), optionalTask.get());

        serverPlayer.sendSystemMessage(Component.literal(result.message()));
        serverPlayer.closeContainer(); //TODO: Add Refresh Menu
    }

    private static void handleSubmitTask(ServerPlayer serverPlayer, SubmitTaskPayload payload) {
        if (!(serverPlayer.containerMenu instanceof BulletinBoardMenu menu)) {
            return;
        }

        if (!menu.hasActiveTask(payload.taskId())) {
            return;
        }

        Optional<TaskDefinition> optionalTask = TaskManager.getTask(payload.taskId());

        if (optionalTask.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("That task no longer exists."));
            serverPlayer.closeContainer();
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(serverPlayer.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(serverPlayer.getUUID());

        if (optionalGroup.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("You are not currently in a delivery group."));
            serverPlayer.closeContainer();
            return;
        }

        TaskDefinition task = optionalTask.get();
        TaskRuntimeManager.SubmissionResult result = TaskRuntimeManager.submitItems(serverPlayer, data, optionalGroup.get(), task);

        serverPlayer.sendSystemMessage(Component.literal(result.message()));

        if (result.completed()) {
            serverPlayer.sendSystemMessage(Component.literal("Rewards: +" + task.rewards().experience() + " XP, +" + task.rewards().money() + " money"));
        }

        serverPlayer.closeContainer(); //TODO: Add Menu Refresh
    }
}