package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.MailboxBlockEntity;
import com.github.spacemex.deliveryquesting.item.DeliveryContainerItem;
import com.github.spacemex.deliveryquesting.menu.entry.MailboxParcelEntry;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MailboxMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final List<MailboxParcelEntry> parcels;
    private static final int OUTBOX_START = 0;
    private static final int OUTBOX_END = MailboxBlockEntity.OUTBOX_SIZE;
    private static final int PLAYER_START = OUTBOX_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private final Container outbox;

    public MailboxMenu(int containerId, Inventory inventory, BlockPos blockPos, List<MailboxParcelEntry> parcels) {
        this(containerId, inventory, blockPos, parcels, new SimpleContainer(MailboxBlockEntity.OUTBOX_SIZE));
    }

    private MailboxMenu(int containerId, Inventory inventory, BlockPos blockPos, List<MailboxParcelEntry> parcels, Container outbox) {
        super(ModMenus.MAILBOX.get(), containerId);

        checkContainerSize(outbox, MailboxBlockEntity.OUTBOX_SIZE);

        this.blockPos = blockPos;
        this.parcels = List.copyOf(parcels);
        this.outbox = outbox;

        outbox.startOpen(inventory.player);

        for (int i = 0; i < MailboxBlockEntity.OUTBOX_SIZE; i++) {
            addSlot(
                    new Slot(outbox, i, 196 + i * 18, 142) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return stack.getItem() instanceof DeliveryContainerItem;
                        }
                    }
            );
        }

        addStandardInventorySlots(inventory, 59, 174);
    }

    public static MailboxMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        List<MailboxParcelEntry> parcels = MailboxParcelEntry.readList(buffer);
        return new MailboxMenu(containerId, inventory, blockPos, parcels);
    }

    public static void open(ServerPlayer player, BlockPos pos, MailboxBlockEntity mailbox) {
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal(
                            "You must be in a delivery group to use a mailbox."
                    )
            );

            return;
        }

        DeliveryGroup group = optionalGroup.get();
        Optional<UUID> boundGroup = mailbox.groupId();

        if (boundGroup.isEmpty()) {
            mailbox.bindToGroup(group.id());
        } else if (!boundGroup.get().equals(group.id())) {
            if (data.getGroup(boundGroup.get()).isPresent()) {
                player.sendSystemMessage(Component.literal("This mailbox belongs to another delivery group."));
                return;
            }

            mailbox.rebindToGroup(group.id());
            player.sendSystemMessage(Component.literal("Reclaimed abandoned mailbox for '" + group.name() + "'."));
        }

        List<MailboxParcelEntry> entries =
                group.mailboxInbox().stream().map(MailboxParcelEntry::from).toList();

        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new MailboxMenu(containerId, inventory, pos, entries, mailbox),
                Component.translatable("screen.delivery_questing.mailbox"));

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
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < OUTBOX_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, OUTBOX_START, OUTBOX_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);

        outbox.stopOpen(player);
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