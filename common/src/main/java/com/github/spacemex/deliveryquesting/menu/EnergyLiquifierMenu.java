
package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
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
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public final class EnergyLiquifierMenu extends AbstractContainerMenu {

    private static final int MACHINE_END =
            EnergyLiquifierBlockEntity.SLOT_COUNT;

    private static final int PLAYER_START = MACHINE_END;
    private static final int PLAYER_END =
            PLAYER_START + Inventory.INVENTORY_SIZE;

    private static final int DATA_COUNT = 3;
    private static final int SWITCH_BUTTON = 0;

    private final BlockPos blockPos;
    private final Container container;
    private final ContainerData data;

    public EnergyLiquifierMenu(int containerId,
                               Inventory inventory,
                               BlockPos blockPos) {
        this(containerId, inventory, blockPos,
                new SimpleContainer(EnergyLiquifierBlockEntity.SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    private EnergyLiquifierMenu(int containerId,
                                Inventory inventory,
                                BlockPos blockPos,
                                Container container,
                                ContainerData data) {
        super(ModMenus.ENERGY_LIQUIFIER.get(), containerId);

        checkContainerSize(container,
                EnergyLiquifierBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.blockPos = blockPos;
        this.container = container;
        this.data = data;

        container.startOpen(inventory.player);

        // Energy item
        addSlot(new Slot(container,
                EnergyLiquifierBlockEntity.ENERGY_ITEM_SLOT,
                53, 36) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return container.canPlaceItem(
                        EnergyLiquifierBlockEntity.ENERGY_ITEM_SLOT,
                        stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Fluid container
        addSlot(new Slot(container,
                EnergyLiquifierBlockEntity.FLUID_ITEM_SLOT,
                107, 36) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return container.canPlaceItem(
                        EnergyLiquifierBlockEntity.FLUID_ITEM_SLOT,
                        stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Upgrade
        addSlot(new Slot(container,
                EnergyLiquifierBlockEntity.UPGRADE_SLOT,
                80, 17) {
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

    public static EnergyLiquifierMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf buffer) {

        return new EnergyLiquifierMenu(
                containerId, inventory, buffer.readBlockPos());
    }

    public static void open(ServerPlayer player,
                            BlockPos pos,
                            EnergyLiquifierBlockEntity machine) {

        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) ->
                        new EnergyLiquifierMenu(
                                containerId,
                                inventory,
                                pos,
                                machine,
                                machine.menuData()),
                Component.translatable(
                        "screen.delivery_questing.energy_liquifier")
        );

        MenuRegistry.openExtendedMenu(
                player, provider, buffer -> buffer.writeBlockPos(pos));
    }

    public int energy() {
        return data.get(0);
    }

    public int fluidAmount() {
        return data.get(1);
    }

    public boolean isReversed() {
        return data.get(2) != 0;
    }

    public int conversionRate() {
        ItemStack upgrade = container.getItem(
                EnergyLiquifierBlockEntity.UPGRADE_SLOT);

        int level = 0;

        if (upgrade.getItem() instanceof UpgradeItem item) {
            level = item.tier().level();
        }

        return switch (level) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 8;
            case 5 -> 16;
            case 6 -> 64;
            default -> 1;
        };
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != SWITCH_BUTTON
                || !stillValid(player)
                || !(container instanceof EnergyLiquifierBlockEntity machine)) {
            return false;
        }

        if (!player.level().isClientSide()) {
            machine.toggleReversed();
            broadcastChanges();
        }

        return true;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(
            @NonNull Player player, int slotIndex) {

        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < MACHINE_END) {
            if (!moveItemStackTo(
                    stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof UpgradeItem) {
            int target = EnergyLiquifierBlockEntity.UPGRADE_SLOT;

            if (!moveItemStackTo(stack, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.BUCKET)
                || stack.getItem() instanceof BucketItem) {
            int target = EnergyLiquifierBlockEntity.FLUID_ITEM_SLOT;

            if (!moveItemStackTo(stack, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Energy items can be placed manually until the
            // platform item-capability integration is added.
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
        if (!player.level().getBlockState(blockPos)
                .is(ModBlocks.ENERGY_LIQUIFIER.get())) {
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
