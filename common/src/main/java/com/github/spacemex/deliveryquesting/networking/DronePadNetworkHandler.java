package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.menu.DronePadMenu;
import com.github.spacemex.deliveryquesting.networking.packets.SubmitDroneDeliveryPayload;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class DronePadNetworkHandler {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SubmitDroneDeliveryPayload.TYPE, SubmitDroneDeliveryPayload.CODEC, (payload, context) -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            context.queue(() -> handleSubmit(player, payload));
        });
    }

    private static void handleSubmit(ServerPlayer player, SubmitDroneDeliveryPayload payload) {
        if (!(player.containerMenu instanceof DronePadMenu menu)) {
            return;
        }

        if (!menu.blockPos().equals(payload.pos())) {
            return;
        }

        if (!(player.level().getBlockEntity(payload.pos()) instanceof DronePadBlockEntity pad)) {
            player.closeContainer();
            return;
        }

        if (!pad.isSkyFree()) {
            player.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.no_sky"));
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.closeContainer();
            return;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!pad.groupId().filter(group.id()::equals).isPresent()) {
            player.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.wrong_group"));
            player.closeContainer();
            return;
        }

        ItemStack boxStack = pad.getItem(0);

        if (!(boxStack.getItem() instanceof CardboardBoxItem boxItem)) {
            return;
        }

        List<ItemStack> contents = boxItem.getContents(boxStack);

        if (contents.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.empty_box"));
            return;
        }

        TaskRuntimeManager.MailboxSubmissionResult result = TaskRuntimeManager.submitDeliveryItems(data, group, contents);

        pad.setItem(0, ItemStack.EMPTY);
        player.sendSystemMessage(Component.literal("Drone delivery sent: " + result.submitted() + " item(s) submitted, " + result.discarded() + " item(s) discarded."));
        player.closeContainer();
    }
}