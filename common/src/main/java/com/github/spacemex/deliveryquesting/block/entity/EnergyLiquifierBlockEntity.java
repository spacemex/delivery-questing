
package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.compat.energy.EnergyLiquifierOutput;
import com.github.spacemex.deliveryquesting.item.UpgradeItem;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("all")
public final class EnergyLiquifierBlockEntity extends BlockEntity implements Container {

    public static final int ENERGY_CAPACITY = 16_000;
    public static final int FLUID_CAPACITY = 16_000;

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(1, ItemStack.EMPTY);

    private int energy;
    private int fluidAmount;
    private boolean reversed;

    public EnergyLiquifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_LIQUIFIER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            EnergyLiquifierBlockEntity machine) {
        if (level.isClientSide()) {
            return;
        }

        int rate = machine.getConversionRate();

        if (machine.reversed) {
            int converted = Math.min(rate,
                    Math.min(machine.fluidAmount,
                            ENERGY_CAPACITY - machine.energy));

            if (converted > 0) {
                machine.fluidAmount -= converted;
                machine.energy += converted;
                machine.setChanged();
            }
        } else {
            int converted = Math.min(rate,
                    Math.min(machine.energy,
                            FLUID_CAPACITY - machine.fluidAmount));

            if (converted > 0) {
                machine.energy -= converted;
                machine.fluidAmount += converted;
                machine.setChanged();
            }
        }

        if (machine.isReversed() && machine.getEnergy() > 0) {
            EnergyLiquifierOutput.push(level, pos, machine);
        }
    }

    public int getConversionRate() {
        return switch (getUpgradeLevel()) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 8;
            case 5 -> 16;
            case 6 -> 64;
            default -> 1;
        };
    }

    public int getUpgradeLevel() {
        ItemStack stack = items.get(0);

        if (stack.getItem() instanceof UpgradeItem upgrade) {
            return upgrade.tier().level();
        }

        return 0;
    }

    public boolean installUpgrade(ItemStack source, Player player) {
        if (!(source.getItem() instanceof UpgradeItem) ||
                !items.get(0).isEmpty()) {
            return false;
        }

        items.set(0, source.copyWithCount(1));

        if (player == null || !player.isCreative()) {
            source.shrink(1);
        }

        setChanged();
        return true;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        if (this.reversed != reversed) {
            this.reversed = reversed;
            setChanged();
        }
    }

    public void toggleReversed() {
        setReversed(!reversed);
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY;
    }

    public void setEnergy(int value) {
        energy = Math.clamp(value, 0, ENERGY_CAPACITY);
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

    public int extractEnergy(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, energy);

        if (!simulate && extracted > 0) {
            energy -= extracted;
            setChanged();
        }

        return extracted;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public int getFluidCapacity() {
        return FLUID_CAPACITY;
    }

    public void setFluidAmount(int value) {
        fluidAmount = Math.clamp(value, 0, FLUID_CAPACITY);
        setChanged();
    }

    public int fillFluid(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int accepted = Math.min(amount, FLUID_CAPACITY - fluidAmount);

        if (!simulate && accepted > 0) {
            fluidAmount += accepted;
            setChanged();
        }

        return accepted;
    }

    public int drainFluid(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int drained = Math.min(amount, fluidAmount);

        if (!simulate && drained > 0) {
            fluidAmount -= drained;
            setChanged();
        }

        return drained;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, items, true);

        output.putInt("energy", energy);
        output.putInt("fluid", fluidAmount);
        output.putBoolean("reversed", reversed);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);

        items.clear();
        ContainerHelper.loadAllItems(input, items);

        energy = Math.clamp(
                input.getIntOr("energy", 0), 0, ENERGY_CAPACITY);

        fluidAmount = Math.clamp(
                input.getIntOr("fluid", 0), 0, FLUID_CAPACITY);

        reversed = input.getBooleanOr("reversed", false);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);

        if (!result.isEmpty()) {
            setChanged();
        }

        return result;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }

        items.set(slot, stack.isEmpty()
                ? ItemStack.EMPTY
                : stack.copyWithCount(1));

        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof UpgradeItem;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos,
                                     @NonNull BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, this);
        }
    }
}
