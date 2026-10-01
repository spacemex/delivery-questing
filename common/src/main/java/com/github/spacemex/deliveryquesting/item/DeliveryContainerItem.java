package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.menu.DeliveryContainerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public final class DeliveryContainerItem extends Item {
    private final int capacity;

    public DeliveryContainerItem(Properties properties, int capacity) {
        super(properties);

        if (capacity <= 0) {
            throw new IllegalArgumentException("Delivery container capacity must be greater than 0");
        }

        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public List<ItemStack> getContents(ItemStack containerStack) {
        NonNullList<ItemStack> contents = NonNullList.withSize(1, ItemStack.EMPTY);

        containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);

        return contents.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    public int getContainedItemCount(ItemStack containerStack) {
        int count = 0;

        for (ItemStack stack : getContents(containerStack)) {
            count += stack.getCount();
        }

        return count;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            DeliveryContainerMenu.open(serverPlayer, hand, this);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        builder.accept(Component.translatable("tooltip.delivery_questing.delivery_container.items", getContainedItemCount(stack), capacity).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.delivery_questing.delivery_container.open").withStyle(ChatFormatting.DARK_GRAY));
    }
}