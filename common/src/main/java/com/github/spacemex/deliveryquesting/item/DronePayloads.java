
package com.github.spacemex.deliveryquesting.item;

import net.minecraft.world.item.ItemStack;

public final class DronePayloads {

    private DronePayloads() {
    }

    public static boolean isSupported(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof CardboardBoxItem ||
                stack.getItem() instanceof BarrelItem);
    }

    public static boolean isBarrel(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BarrelItem;
    }

    public static int getTier(ItemStack stack) {
        if (stack.getItem() instanceof CardboardBoxItem box) {
            return box.tier().level();
        }

        if (stack.getItem() instanceof BarrelItem barrel) {
            return barrel.tier().level();
        }

        return 1;
    }
}
