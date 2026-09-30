package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailContractPayload;
import com.github.spacemex.deliveryquesting.networking.packets.MarkEmailReadPayload;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.GroupEmail;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class ComputerNetworkHandler {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        registerMarkEmailRead();
        registerAcceptEmailContract();
    }

    private static void registerMarkEmailRead() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, MarkEmailReadPayload.TYPE, MarkEmailReadPayload.CODEC,
                (payload, context) -> {
                    if (!(context.getPlayer() instanceof ServerPlayer player)) {
                        return;
                    }
                    context.queue(() -> handleMarkEmailRead(player, payload));
                });
    }

    private static void registerAcceptEmailContract() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AcceptEmailContractPayload.TYPE, AcceptEmailContractPayload.CODEC,
                (payload, context) -> {
                    if (!(context.getPlayer() instanceof ServerPlayer player)) {
                        return;
                    }
                    context.queue(() -> handleAcceptEmailContract(player, payload));
                });
    }

    private static void handleMarkEmailRead(ServerPlayer player, MarkEmailReadPayload payload) {
        if (!(player.containerMenu instanceof ComputerMenu menu)) {
            return;
        }

        if (!menu.hasMail(payload.emailId())) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            return;
        }

        data.markEmailRead(optionalGroup.get().id(), payload.emailId());
    }

    private static void handleAcceptEmailContract(ServerPlayer player, AcceptEmailContractPayload payload) {
        if (!(player.containerMenu instanceof ComputerMenu menu)) {
            return;
        }

        if (!menu.hasMail(payload.emailId())) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.closeContainer();
            return;
        }

        DeliveryGroup group = optionalGroup.get();
        Optional<GroupEmail> optionalEmail = group.getEmail(payload.emailId());

        if (optionalEmail.isEmpty()) {
            player.closeContainer();
            return;
        }

        GroupEmail email = optionalEmail.get();

        if (email.type() != GroupEmail.Type.CONTRACT) {
            player.closeContainer();
            return;
        }

        Optional<TaskDefinition> optionalTask = TaskManager.getTask(email.referenceId());

        if (optionalTask.isEmpty()) {
            player.sendSystemMessage(Component.literal("That contract no longer exists."));
            player.closeContainer();
            return;
        }

        data.markEmailRead(group.id(), email.id());

        TaskRuntimeManager.ActionResult result = TaskRuntimeManager.acceptTask(data, group, optionalTask.get());

        player.sendSystemMessage(Component.literal(result.message()));
        player.closeContainer();
    }
}