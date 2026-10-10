package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.item.DeliveryContainerStorage;
import com.github.spacemex.deliveryquesting.item.UpgradeItem;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public final class PackagerBlockEntity extends BlockEntity implements Container {

    public static final int INPUT_SLOT = 0;
    public static final int CONTAINER_SLOT = 1;
    public static final int UPGRADE_SLOT = 2;

    public static final int SLOT_COUNT = 3;

    public static final int ENERGY_CAPACITY = 16_000;

    public static final int ITEM_ENERGY_USAGE = 1_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int energy;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) {
                return energy;
            }

            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public PackagerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PACKAGER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PackagerBlockEntity packager) {
        if (level.isClientSide()) {
            return;
        }

        if (level.getGameTime() % 4L != 0L) {
            return;
        }

        if (packager.energy < ITEM_ENERGY_USAGE) {
            return;
        }

        ItemStack source = packager.getItem(INPUT_SLOT);

        if (source.isEmpty()) {
            return;
        }

        ItemStack container = packager.getItem(CONTAINER_SLOT);

        if (!DeliveryContainerStorage.isDeliveryContainer(container)) {
            return;
        }

        int requested = Math.min(packager.getItemTransferCount(), source.getCount());

        int inserted = DeliveryContainerStorage.insert(container, source, requested);

        if (inserted <= 0) {
            return;
        }

        source.shrink(inserted);

        if (source.isEmpty()) {
            packager.items.set(INPUT_SLOT, ItemStack.EMPTY);
        }

        packager.useEnergy(ITEM_ENERGY_USAGE);

        packager.setChanged();
    }

    public ContainerData menuData() {
        return menuData;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(ENERGY_CAPACITY, energy));

        setChanged();
    }

    public int receiveEnergy(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int accepted = Math.min(amount, ENERGY_CAPACITY - energy);

        if (!simulate && accepted > 0) {

            energy += accepted;

            setChanged();
        }

        return accepted;
    }

    private int useEnergy(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int used = Math.min(energy, amount);

        if (used > 0) {
            energy -= used;

            setChanged();
        }

        return used;
    }

    public int getUpgradeLevel() {
        ItemStack stack = items.get(UPGRADE_SLOT);

        if (stack.getItem() instanceof UpgradeItem upgrade) {
            return upgrade.tier().level();
        }

        return 0;
    }

    public int getItemTransferCount() {
        return 1 << Math.min(getUpgradeLevel(), 6);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, items, true);

        output.putInt("energy", energy);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);

        items.clear();

        ContainerHelper.loadAllItems(input, items);

        energy = Math.max(0, Math.min(ENERGY_CAPACITY, input.getIntOr("energy", 0)));
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);

        if (!stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(items, slot);

        setChanged();

        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }

        stack.limitSize(getMaxStackSize(stack));

        items.set(slot, stack);

        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return switch (slot) {
            case INPUT_SLOT -> DeliveryContainerStorage.canStoreInput(stack);
            case CONTAINER_SLOT -> DeliveryContainerStorage.isDeliveryContainer(stack);
            case UPGRADE_SLOT -> stack.getItem() instanceof UpgradeItem;
            default -> false;
        };
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        setChanged();
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, this);
        }
    }
}