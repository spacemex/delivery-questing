package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.PackagerBlockEntity;
import com.github.spacemex.deliveryquesting.item.DeliveryContainerStorage;
import com.github.spacemex.deliveryquesting.item.UpgradeItem;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class PackagerMenu extends AbstractContainerMenu {

    private static final int MACHINE_END = PackagerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private static final int DATA_COUNT = 1;

    private final BlockPos blockPos;

    private final Container container;

    private final ContainerData data;

    public PackagerMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        this(containerId, inventory, blockPos, new SimpleContainer(PackagerBlockEntity.SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    private PackagerMenu(int containerId, Inventory inventory, BlockPos blockPos, Container container,
                         ContainerData data) {
        super(ModMenus.PACKAGER.get(), containerId);

        checkContainerSize(container, PackagerBlockEntity.SLOT_COUNT);

        checkContainerDataCount(data, DATA_COUNT);

        this.blockPos = blockPos;

        this.container = container;

        this.data = data;

        container.startOpen(inventory.player);

        addSlot(new Slot(container, PackagerBlockEntity.INPUT_SLOT, 79, 36) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return DeliveryContainerStorage.canStoreInput(stack);
            }
        });

        addSlot(new Slot(container, PackagerBlockEntity.CONTAINER_SLOT, 133, 36) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return DeliveryContainerStorage.isDeliveryContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addSlot(new Slot(container, PackagerBlockEntity.UPGRADE_SLOT, 7, 17) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.getItem()
                        instanceof UpgradeItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addStandardInventorySlots(inventory, 8, 84);

        addDataSlots(data);
    }

    public static PackagerMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new PackagerMenu(containerId, inventory, buffer.readBlockPos());
    }

    public static void open(ServerPlayer player, BlockPos pos, PackagerBlockEntity packager) {
        SimpleMenuProvider provider =
                new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                        new PackagerMenu(containerId, inventory, pos, packager, packager.menuData()),
                        Component.translatable("screen.delivery_questing.packager"));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> buffer.writeBlockPos(pos));
    }

    public int energy() {
        return data.get(0);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < MACHINE_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }

        } else if (stack.getItem() instanceof UpgradeItem) {
            if (!moveItemStackTo(stack, PackagerBlockEntity.UPGRADE_SLOT, PackagerBlockEntity.UPGRADE_SLOT + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (DeliveryContainerStorage.isDeliveryContainer(stack)) {
            if (!moveItemStackTo(stack, PackagerBlockEntity.CONTAINER_SLOT,
                    PackagerBlockEntity.CONTAINER_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }

        } else if (DeliveryContainerStorage.canStoreInput(stack)) {
            if (!moveItemStackTo(stack, PackagerBlockEntity.INPUT_SLOT, PackagerBlockEntity.INPUT_SLOT + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
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
        if (!player.level().getBlockState(blockPos).is(ModBlocks.PACKAGER.get())) {
            return false;
        }

        return player.distanceToSqr(
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64D;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);

        container.stopOpen(player);
    }
}