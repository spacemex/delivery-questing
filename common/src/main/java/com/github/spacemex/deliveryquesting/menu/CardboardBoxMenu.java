package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.CardboardBoxBlock;
import com.github.spacemex.deliveryquesting.block.entity.CardboardBoxBlockEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.item.CardboardBoxTier;
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
    private final BlockPos blockPos;
    private final Container container;
    private final CardboardBoxTier tier;
    private final int boxEnd;
    private final int playerStart;
    private final int playerEnd;

    public CardboardBoxMenu(int containerId, Inventory inventory, BlockPos blockPos, CardboardBoxTier tier) {
        this(containerId, inventory, blockPos, tier, new SimpleContainer(tier.slots()));
    }

    private CardboardBoxMenu(int containerId, Inventory inventory, BlockPos blockPos, CardboardBoxTier tier, Container container) {
        super(ModMenus.CARDBOARD_BOX.get(), containerId);

        checkContainerSize(container, tier.slots());

        this.blockPos = blockPos;
        this.container = container;
        this.tier = tier;
        this.boxEnd = tier.slots();
        this.playerStart = boxEnd;
        this.playerEnd = playerStart + Inventory.INVENTORY_SIZE;

        container.startOpen(inventory.player);

        int startX = boxSlotStartX();
        int startY = boxSlotStartY();

        for (int i = 0; i < tier.slots(); i++) {
            int column = i % tier.columns();
            int row = i / tier.columns();

            addSlot(
                    new Slot(container, i, startX + column * 18, startY + row * 18) {
                        @Override
                        public boolean mayPlace(@NonNull ItemStack stack) {
                            return CardboardBoxItem.canStore(stack);
                        }
                    });
        }
        addStandardInventorySlots(inventory, 8, playerInventoryY());
    }

    public static CardboardBoxMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        CardboardBoxTier tier = CardboardBoxTier.fromLevel(buffer.readVarInt());
        return new CardboardBoxMenu(containerId, inventory, blockPos, tier);
    }

    public static void open(ServerPlayer player, BlockPos pos, CardboardBoxBlockEntity box) {
        CardboardBoxTier tier = box.tier();
        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new CardboardBoxMenu(containerId, inventory, pos, tier, box),
                Component.translatable("block.delivery_questing.cardboard_box_tier_" + tier.level()));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> {
            buffer.writeBlockPos(pos);
            buffer.writeVarInt(tier.level());
        });
    }

    public CardboardBoxTier tier() {
        return tier;
    }

    public int boxSlotStartX() {
        return 8 + (9 - tier.columns()) * 9;
    }

    public int boxSlotStartY() {
        return 18;
    }

    public int playerInventoryY() {
        return 48 + tier.rows() * 18;
    }

    public int imageHeight() {
        return 130 + tier.rows() * 18;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < boxEnd) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!CardboardBoxItem.canStore(stack)) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(stack, 0, boxEnd, false)) {
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
        if (!(player.level().getBlockState(blockPos).getBlock() instanceof CardboardBoxBlock box)) {
            return false;
        }

        if (box.tier() != tier) {
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