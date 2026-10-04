package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.CardboardBoxBlockEntity;
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

public final class CardboardBoxMenu extends AbstractContainerMenu {
    private static final int BOX_START = 0;
    private static final int BOX_END = 1;
    private static final int PLAYER_START = BOX_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private final BlockPos blockPos;
    private final Container container;

    public CardboardBoxMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        this(containerId, inventory, blockPos, new SimpleContainer(CardboardBoxBlockEntity.SLOT_COUNT));
    }

    private CardboardBoxMenu(int containerId, Inventory inventory, BlockPos blockPos, Container container) {
        super(ModMenus.CARDBOARD_BOX_TIER_1.get(), containerId);

        checkContainerSize(container, CardboardBoxBlockEntity.SLOT_COUNT);

        this.blockPos = blockPos;
        this.container = container;

        container.startOpen(inventory.player);

        addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return CardboardBoxItem.canStore(stack);
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
    }

    public static CardboardBoxMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        return new CardboardBoxMenu(containerId, inventory, blockPos);
    }

    public static void open(ServerPlayer player, BlockPos pos, CardboardBoxBlockEntity box) {
        SimpleMenuProvider provider =
                new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                        new CardboardBoxMenu(containerId, inventory, pos, box),
                        Component.translatable("screen.delivery_questing.cardboard_box_tier_1"));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < BOX_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }

        } else {
            if (!CardboardBoxItem.canStore(stack)) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(stack, BOX_START, BOX_END, false)) {
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
        if (!player.level().getBlockState(blockPos).is(ModBlocks.CARDBOARD_BOX_TIER_1.get())) {
            return false;
        }

        double x = blockPos.getX() + 0.5D;
        double y = blockPos.getY() + 0.5D;
        double z = blockPos.getZ() + 0.5D;

        return player.distanceToSqr(x, y, z) <= 64.0D;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}