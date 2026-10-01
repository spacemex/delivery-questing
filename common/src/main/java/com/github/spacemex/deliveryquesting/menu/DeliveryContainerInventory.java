package com.github.spacemex.deliveryquesting.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class DeliveryContainerInventory extends SimpleContainer {
    private final ItemStack containerStack;
    private final int capacity;

    public DeliveryContainerInventory(ItemStack containerStack, int capacity) {
        super(1);

        this.containerStack = containerStack;
        this.capacity = capacity;

        ItemContainerContents contents = containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        contents.copyInto(getItems());

        ItemStack content = getItem(0);

        if (!content.isEmpty() && content.getCount() > capacity) {

            content = content.copy();
            content.setCount(capacity);

            super.setItem(0, content);
            setChanged();
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack copy = stack.copy();

        if (!copy.isEmpty() && copy.getCount() > capacity) {
            copy.setCount(capacity);
        }

        super.setItem(slot, copy);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }
}