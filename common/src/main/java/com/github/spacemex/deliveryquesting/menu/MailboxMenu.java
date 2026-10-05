package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.MailboxBlockEntity;
import com.github.spacemex.deliveryquesting.item.DeliveryContainerItem;
import com.github.spacemex.deliveryquesting.menu.entry.MailboxParcelEntry;
import com.github.spacemex.deliveryquesting.networking.MailboxNetworkHandler;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModItems;
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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MailboxMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final List<MailboxParcelEntry> parcels;
    private static final int INBOX_START = 0;
    private static final int INBOX_END = 4;
    private static final int OUTBOX_START = INBOX_END;
    private static final int OUTBOX_END = OUTBOX_START + MailboxBlockEntity.OUTBOX_SIZE;
    private static final int PLAYER_START = OUTBOX_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private final Container inbox;
    private final Container outbox;

    public MailboxMenu(int containerId, Inventory inventory, BlockPos blockPos, List<MailboxParcelEntry> parcels) {
        this(containerId, inventory, blockPos, parcels, createInbox(parcels), new SimpleContainer(MailboxBlockEntity.OUTBOX_SIZE));
    }

    private MailboxMenu(int containerId, Inventory inventory, BlockPos blockPos, List<MailboxParcelEntry> parcels, Container inbox, Container outbox) {
        super(ModMenus.MAILBOX.get(), containerId);

        checkContainerSize(inbox, 4);
        checkContainerSize(outbox, MailboxBlockEntity.OUTBOX_SIZE);

        this.blockPos = blockPos;
        this.parcels = List.copyOf(parcels);
        this.inbox = inbox;
        this.outbox = outbox;

        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(inbox, i, 8 + i * 18, 46) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return false;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        for (int i = 0; i < MailboxBlockEntity.OUTBOX_SIZE; i++) {
            addSlot(new Slot(outbox, i, 98 + i * 18, 46) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof DeliveryContainerItem;
                }
            });
        }

        addStandardInventorySlots(inventory, 8, 77);
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
                new MailboxMenu(containerId, inventory, pos, entries, createInbox(entries), mailbox),
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
        if (slotIndex >= INBOX_START && slotIndex < INBOX_END) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex >= OUTBOX_START && slotIndex < OUTBOX_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!(stack.getItem() instanceof DeliveryContainerItem)) {
                return ItemStack.EMPTY;
            }

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

    private static Container createInbox(List<MailboxParcelEntry> parcels) {
        SimpleContainer inbox = new SimpleContainer(4);

        for (int i = 0; i < Math.min(parcels.size(), 4); i++) {
            MailboxParcelEntry parcel = parcels.get(i);

            ItemStack displayStack = new ItemStack(parcel.contractEnvelope() ? ModItems.SEALED_ENVELOPE.get() : ModItems.SEALED_PARCEL.get());
            inbox.setItem(i, displayStack);
        }

        return inbox;
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, @NonNull ContainerInput input, @NonNull Player player) {
        if (slotIndex >= INBOX_START && slotIndex < INBOX_END) {
            int inboxIndex = slotIndex - INBOX_START;

            if (inboxIndex >= parcels.size()) {
                return;
            }

            if (inbox.getItem(inboxIndex).isEmpty()) {
                return;
            }

            if (input != ContainerInput.PICKUP && input != ContainerInput.QUICK_MOVE) {
                return;
            }

            if (player.level().isClientSide()) {
                return;
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            MailboxParcelEntry parcel = parcels.get(inboxIndex);

            if (MailboxNetworkHandler.collectParcel(serverPlayer, parcel.id())) {

                inbox.setItem(inboxIndex, ItemStack.EMPTY);
                broadcastChanges();
            }
            return;
        }

        super.clicked(slotIndex, buttonNum, input, player);
    }


}