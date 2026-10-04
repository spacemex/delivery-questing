package com.github.spacemex.deliveryquesting.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public final class CardboardBoxItem extends BlockItem {

    public static final int SLOT_COUNT = 1;

    public CardboardBoxItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    public static List<ItemStack> getContents(ItemStack stack) {
        NonNullList<ItemStack> contents = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);
        return contents.stream().filter(item -> !item.isEmpty()).map(ItemStack::copy).toList();
    }

    public static boolean canStore(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().canFitInsideContainerItems();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        int occupied = getContents(stack).size();
        builder.accept(Component.translatable("tooltip.delivery_questing.cardboard_box.stacks", occupied, SLOT_COUNT).withStyle(ChatFormatting.GRAY));
    }
}