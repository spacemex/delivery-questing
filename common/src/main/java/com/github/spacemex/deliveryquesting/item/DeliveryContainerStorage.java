package com.github.spacemex.deliveryquesting.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class DeliveryContainerStorage {

    private DeliveryContainerStorage() {
    }

    public static boolean isDeliveryContainer(ItemStack stack) {
        return stack.getItem() instanceof DeliveryContainerItem || stack.getItem() instanceof CardboardBoxItem;
    }

    public static boolean canStoreInput(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().canFitInsideContainerItems();
    }

    public static int insert(ItemStack containerStack, ItemStack source, int maxAmount) {
        if (containerStack.isEmpty() || source.isEmpty() || maxAmount <= 0 || !canStoreInput(source)) {
            return 0;
        }

        if (containerStack.getItem() instanceof DeliveryContainerItem containerItem) {
            return insertIntoDeliveryContainer(containerStack, containerItem, source, maxAmount);
        }

        if (containerStack.getItem() instanceof CardboardBoxItem boxItem) {
            return insertIntoCardboardBox(containerStack, boxItem, source, maxAmount);
        }

        return 0;
    }

    private static int insertIntoDeliveryContainer(ItemStack containerStack, DeliveryContainerItem containerItem,
                                                   ItemStack source, int maxAmount) {
        NonNullList<ItemStack> contents = NonNullList.withSize(1, ItemStack.EMPTY);

        containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);

        ItemStack existing = contents.getFirst();

        int requested = Math.min(maxAmount, source.getCount());

        if (existing.isEmpty()) {
            int inserted = Math.min(requested, Math.min(containerItem.capacity(), source.getMaxStackSize()));

            if (inserted <= 0) {
                return 0;
            }

            ItemStack stored = source.copyWithCount(inserted);

            contents.set(0, stored);

            containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

            return inserted;
        }

        if (!ItemStack.isSameItemSameComponents(existing, source)) {
            return 0;
        }

        int maxStack = Math.min(containerItem.capacity(), existing.getMaxStackSize());

        int space = maxStack - existing.getCount();

        if (space <= 0) {
            return 0;
        }

        int inserted = Math.min(requested, space);

        existing.grow(inserted);

        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

        return inserted;
    }

    private static int insertIntoCardboardBox(ItemStack containerStack, CardboardBoxItem boxItem, ItemStack source,
                                              int maxAmount) {
        if (!CardboardBoxItem.canStore(source)) {
            return 0;
        }

        NonNullList<ItemStack> contents = NonNullList.withSize(boxItem.tier().slots(), ItemStack.EMPTY);

        containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);

        int requested = Math.min(maxAmount, source.getCount());

        int remaining = requested;

        for (int i = 0; i < contents.size() && remaining > 0; i++) {
            ItemStack existing = contents.get(i);

            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, source)) {
                continue;
            }

            int space = existing.getMaxStackSize() - existing.getCount();

            if (space <= 0) {
                continue;
            }

            int moved = Math.min(space, remaining);

            existing.grow(moved);

            remaining -= moved;
        }

        for (int i = 0; i < contents.size() && remaining > 0; i++) {
            if (!contents.get(i).isEmpty()) {
                continue;
            }

            int moved = Math.min(source.getMaxStackSize(), remaining);

            contents.set(i, source.copyWithCount(moved));

            remaining -= moved;
        }

        int inserted = requested - remaining;

        if (inserted <= 0) {
            return 0;
        }

        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

        return inserted;
    }
}