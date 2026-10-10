package com.github.spacemex.deliveryquesting.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DisplayHelpers {

    private DisplayHelpers() {
    }

    public static Component getItemDisplayName(Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);

        if (item == null || item == Items.AIR) {
            return Component.literal(itemId.toString());
        }

        return new ItemStack(item).getHoverName();
    }

    public static Component getItemDisplayName(ItemStack stack) {
        return stack.getHoverName();
    }

    public static Component getItemDisplayName(Item item) {
        return new ItemStack(item).getHoverName();
    }


    public static Component getFluidDisplayName(Identifier fluidId) {
        String fluidIdString = fluidId.toString();
        String fluidIdStringCleaned = fluidIdString.replace(":", ".");

        return Component.translatable("fluid." + fluidIdStringCleaned);
    }


}
