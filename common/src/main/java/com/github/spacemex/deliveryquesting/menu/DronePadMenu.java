package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
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

public final class DronePadMenu extends AbstractContainerMenu {
    private static final int PAYLOAD_END = 1;
    private static final int PLAYER_START = PAYLOAD_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private final BlockPos blockPos;
    private final Container container;

    public DronePadMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        this(containerId, inventory, blockPos, new SimpleContainer(DronePadBlockEntity.PAYLOAD_SLOTS));
    }

    private DronePadMenu(int containerId, Inventory inventory, BlockPos blockPos, Container container) {
        super(ModMenus.DRONE_PAD.get(), containerId);

        checkContainerSize(container, DronePadBlockEntity.PAYLOAD_SLOTS);

        this.blockPos = blockPos;
        this.container = container;

        container.startOpen(inventory.player);

        addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.getItem() instanceof CardboardBoxItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addStandardInventorySlots(inventory, 8, 84);
    }

    public static DronePadMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new DronePadMenu(containerId, inventory, buffer.readBlockPos());
    }

    public static void open(ServerPlayer player, BlockPos pos, DronePadBlockEntity pad) {
        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new DronePadMenu(containerId, inventory, pos, pad),
                Component.translatable("screen.delivery_questing.drone_pad"));

        MenuRegistry.openExtendedMenu(player, provider, buffer ->
                buffer.writeBlockPos(pos));
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public boolean hasPayload() {
        return getSlot(0).hasItem();
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < PAYLOAD_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }

        } else {
            if (!(stack.getItem() instanceof CardboardBoxItem)) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(stack, 0, PAYLOAD_END, false)) {
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
        if (!player.level().getBlockState(blockPos).is(ModBlocks.DRONE_PAD.get())) {
            return false;
        }

        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}