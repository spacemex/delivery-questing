package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MailboxMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final List<MailboxParcelEntry> parcels;

    public MailboxMenu(int containerId, Inventory inventory, BlockPos blockPos, List<MailboxParcelEntry> parcels) {
        super(ModMenus.MAILBOX.get(), containerId);
        this.blockPos = blockPos;
        this.parcels = List.copyOf(parcels);
    }

    public static MailboxMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        List<MailboxParcelEntry> parcels = MailboxParcelEntry.readList(buffer);
        return new MailboxMenu(containerId, inventory, blockPos, parcels);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.sendSystemMessage(Component.literal("You must be in a delivery group to use a mailbox."));
            return;
        }

        DeliveryGroup group = optionalGroup.get();
        List<MailboxParcelEntry> entries = group.mailboxInbox().stream().map(MailboxParcelEntry::from).toList();
        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new MailboxMenu(containerId, inventory, pos, entries), Component.translatable("screen.delivery_questing.mailbox"));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> {
            buffer.writeBlockPos(pos);
            MailboxParcelEntry.writeList(buffer, entries);
        });
    }

    public List<MailboxParcelEntry> parcels() {
        return parcels;
    }

    public boolean hasParcel(UUID parcelId) {
        return parcels.stream().anyMatch(parcel -> parcel.id().equals(parcelId));
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.level().getBlockState(blockPos).is(ModBlocks.MAILBOX.get())) {
            return false;
        }

        double x = blockPos.getX() + 0.5D;
        double y = blockPos.getY() + 0.5D;
        double z = blockPos.getZ() + 0.5D;

        return player.distanceToSqr(x, y, z) <= 64.0D;
    }
}