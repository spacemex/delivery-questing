package com.github.spacemex.deliveryquesting.networking;

import com.github.spacemex.deliveryquesting.item.SealedParcelItem;
import com.github.spacemex.deliveryquesting.menu.MailboxMenu;
import com.github.spacemex.deliveryquesting.networking.packets.CollectParcelPayload;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.MailboxParcel;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class MailboxNetworkHandler {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CollectParcelPayload.TYPE, CollectParcelPayload.CODEC, (payload, context) -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }

            context.queue(() -> collectParcel(player, payload));
        });
    }

    private static void collectParcel(ServerPlayer player, CollectParcelPayload payload) {
        if (!(player.containerMenu instanceof MailboxMenu menu)) {
            return;
        }

        if (!menu.hasParcel(payload.parcelId())) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not currently in a delivery group."));
            player.closeContainer();
            return;
        }

        DeliveryGroup group = optionalGroup.get();
        Optional<MailboxParcel> optionalParcel = data.collectMailboxParcel(group.id(), payload.parcelId());

        if (optionalParcel.isEmpty()) {
            player.sendSystemMessage(Component.literal("That parcel is no longer in the mailbox."));
            player.closeContainer();
            return;
        }

        MailboxParcel parcel = optionalParcel.get();
        ItemStack stack = SealedParcelItem.create(parcel);

        player.getInventory().add(stack);

        if (!stack.isEmpty()) {
            player.drop(stack, false, false);
        }

        player.sendSystemMessage(Component.literal("Collected parcel from " + parcel.sender() + "."));
        player.closeContainer();
    }
}