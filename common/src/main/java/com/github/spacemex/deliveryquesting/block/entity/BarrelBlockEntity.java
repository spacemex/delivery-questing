
package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.block.BarrelBlock;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.github.spacemex.deliveryquesting.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public final class BarrelBlockEntity extends BlockEntity {

    private final int capacity;

    private BarrelContents contents = BarrelContents.EMPTY;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL.get(), pos, state);

        if (!(state.getBlock() instanceof BarrelBlock barrel)) {
            throw new IllegalArgumentException("BarrelBlockEntity created for a non-barrel block");
        }

        capacity = barrel.tier().capacity();
    }

    public int getCapacity() {
        return capacity;
    }

    public BarrelContents getContents() {
        return contents;
    }

    public int fill(Identifier fluid, int requested, boolean simulate) {
        if (requested <= 0 || fluid.equals(BarrelContents.EMPTY_FLUID)) {
            return 0;
        }

        if (!contents.isEmpty() && !contents.fluid().equals(fluid)) {
            return 0;
        }

        int accepted = Math.min(requested, capacity - contents.amount());

        if (!simulate && accepted > 0) {
            contents = new BarrelContents(fluid, contents.amount() + accepted);
            setChanged();
        }

        return accepted;
    }

    public int drain(int requested, boolean simulate) {
        if (requested <= 0 || contents.isEmpty()) {
            return 0;
        }

        int drained = Math.min(requested, contents.amount());

        if (!simulate && drained > 0) {
            contents = new BarrelContents(contents.fluid(), contents.amount() - drained);
            setChanged();
        }

        return drained;
    }

    private void setContents(BarrelContents value) {
        int amount = Math.min(capacity, value.amount());

        contents = amount <= 0 ? BarrelContents.EMPTY : new BarrelContents(value.fluid(), amount);

        setChanged();
    }

    public void restoreContents(BarrelContents snapshot) {
        setContents(snapshot);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("fluid", BarrelContents.CODEC, contents);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);

        contents = input.read("fluid", BarrelContents.CODEC).orElse(BarrelContents.EMPTY);

        if (contents.amount() > capacity) {
            contents = new BarrelContents(contents.fluid(), capacity);
        }
    }

    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter components) {
        super.applyImplicitComponents(components);

        setContents(components.getOrDefault(ModDataComponents.BARREL_CONTENTS.get(), BarrelContents.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NonNull Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.BARREL_CONTENTS.get(), contents);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void removeComponentsFromTag(@NonNull ValueOutput output) {
        output.discard("fluid");
    }
}
