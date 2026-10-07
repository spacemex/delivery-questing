package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
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

public final class DronePadMenu extends AbstractContainerMenu {

    private static final int MACHINE_END = DronePadBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_END;
    private static final int PLAYER_END = PLAYER_START + Inventory.INVENTORY_SIZE;
    private static final int DATA_COUNT = 3;

    private final BlockPos blockPos;

    private final Container container;

    private final ContainerData data;

    public DronePadMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        this(containerId, inventory, blockPos, new SimpleContainer(DronePadBlockEntity.SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    private DronePadMenu(int containerId, Inventory inventory, BlockPos blockPos, Container container,
                         ContainerData data) {
        super(ModMenus.DRONE_PAD.get(), containerId);

        checkContainerSize(container, DronePadBlockEntity.SLOT_COUNT);

        checkContainerDataCount(data, DATA_COUNT);

        this.blockPos = blockPos;

        this.container = container;

        this.data = data;

        container.startOpen(inventory.player);

        addSlot(new Slot(container, DronePadBlockEntity.PAYLOAD_SLOT, 53, 36) {

            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.getItem() instanceof CardboardBoxItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addSlot(new Slot(container, DronePadBlockEntity.UPGRADE_SLOT, 80, 59) {

            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.getItem() instanceof UpgradeItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addStandardInventorySlots(inventory, 8, 84);

        addDataSlots(data);
    }

    public static DronePadMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new DronePadMenu(containerId, inventory, buffer.readBlockPos());
    }

    public static void open(ServerPlayer player, BlockPos pos, DronePadBlockEntity pad) {
        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new DronePadMenu(containerId, inventory, pos, pad, pad.menuData()),
                Component.translatable("screen.delivery_questing.drone_pad"));

        MenuRegistry.openExtendedMenu(player, provider, buffer ->
                buffer.writeBlockPos(pos));
    }

    public int padEnergy() {
        return data.get(0);
    }

    public int droneEnergy() {
        return data.get(1);
    }

    public int droneTier() {
        return data.get(2);
    }

    public boolean isDroneFullyCharged() {
        return droneEnergy() >= DroneEntity.ENERGY_CAPACITY;
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

        if (slotIndex < MACHINE_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof CardboardBoxItem) {
            if (!moveItemStackTo(stack, DronePadBlockEntity.PAYLOAD_SLOT, DronePadBlockEntity.PAYLOAD_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof UpgradeItem) {
            if (!moveItemStackTo(stack, DronePadBlockEntity.UPGRADE_SLOT, DronePadBlockEntity.UPGRADE_SLOT + 1, false)) {
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