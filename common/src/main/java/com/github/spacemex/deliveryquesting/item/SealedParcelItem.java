package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.progression.entry.MailboxParcel;
import com.github.spacemex.deliveryquesting.registry.ModDataComponents;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import com.github.spacemex.deliveryquesting.task.entry.ItemReward;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class SealedParcelItem extends Item {

    public SealedParcelItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(MailboxParcel parcel) {
        ItemStack stack = new ItemStack(ModItems.SEALED_PARCEL.get());

        stack.set(ModDataComponents.MAILBOX_PARCEL.get(), parcel);

        return stack;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        MailboxParcel parcel = stack.get(ModDataComponents.MAILBOX_PARCEL.get());

        if (parcel == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        for (ItemReward reward : parcel.items()) {
            giveReward(player, reward);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public static void giveReward(Player player, ItemReward reward) {
        Item item = BuiltInRegistries.ITEM.getValue(reward.item());

        if (item == null || item == Items.AIR) {
            DeliveryQuesting.LOGGER.error("Mailbox parcel contains invalid reward item {}", reward.item());

            return;
        }

        int remaining = reward.count();

        ItemStack example = new ItemStack(item);

        int maxStackSize = example.getMaxStackSize();

        while (remaining > 0) {
            int amount = Math.min(remaining, maxStackSize);

            ItemStack rewardStack = new ItemStack(item, amount);

            player.getInventory().add(rewardStack);

            if (!rewardStack.isEmpty()) {
                player.drop(rewardStack, false, false);
            }

            remaining -= amount;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        MailboxParcel parcel = stack.get(ModDataComponents.MAILBOX_PARCEL.get());

        if (parcel == null) {
            return;
        }

        Component sender = Component.literal(parcel.sender()).withStyle(ChatFormatting.DARK_BLUE);

        builder.accept(Component.translatable("tooltip.delivery_questing.by", sender)
                .withStyle(ChatFormatting.DARK_BLUE));

        builder.accept(Component.translatable("tooltip.delivery_questing.sealed_parcel.open")
                .withStyle(ChatFormatting.GRAY));

        builder.accept(Component.translatable("tooltip.delivery_questing.item_count", parcel.itemCount())
                .withStyle(ChatFormatting.GRAY));
    }
}
