package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.item.DeliveryContainerItem;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class DeliveryContainerMenu extends AbstractContainerMenu {
    private static final int CONTAINER_START = 0;
    private static final int CONTAINER_END = 1;
    private static final int PLAYER_START = CONTAINER_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private final InteractionHand hand;
    private final int capacity;

    public DeliveryContainerMenu(int containerId, Inventory inventory, InteractionHand hand, int capacity) {
        this(containerId, inventory, hand, capacity, new SimpleContainer(1));
    }

    private DeliveryContainerMenu(int containerId, Inventory inventory, InteractionHand hand, int capacity, Container container) {
        super(ModMenus.DELIVERY_CONTAINER.get(), containerId);

        checkContainerSize(container, 1);

        this.hand = hand;
        this.capacity = capacity;

        container.startOpen(inventory.player);

        addSlot(
                new Slot(container, 0, 80, 35) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem().canFitInsideContainerItems();
                    }

                    @Override
                    public int getMaxStackSize() {
                        return capacity;
                    }
                });

        addStandardInventorySlots(inventory, 8, 84);
    }

    public static DeliveryContainerMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int capacity = buffer.readVarInt();

        return new DeliveryContainerMenu(containerId, inventory, hand, capacity);
    }

    public static void open(ServerPlayer player, InteractionHand hand, DeliveryContainerItem item) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() != item) {
            return;
        }

        DeliveryContainerInventory container = new DeliveryContainerInventory(stack, item.capacity());

        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new DeliveryContainerMenu(containerId, inventory, hand, item.capacity(), container), stack.getHoverName());

        MenuRegistry.openExtendedMenu(player, provider, buffer -> {
            buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
            buffer.writeVarInt(item.capacity());
        });
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < CONTAINER_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }

        } else {
            if (!moveItemStackTo(stack, CONTAINER_START, CONTAINER_END, false)) {
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
    public boolean stillValid(Player player) {
        ItemStack held = player.getItemInHand(hand);

        if (!(held.getItem() instanceof DeliveryContainerItem item)) {
            return false;
        }

        return item.capacity() == capacity;
    }
}