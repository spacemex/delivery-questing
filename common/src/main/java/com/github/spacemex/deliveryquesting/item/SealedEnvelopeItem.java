package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.progression.MailboxParcel;
import com.github.spacemex.deliveryquesting.registry.ModDataComponents;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import com.github.spacemex.deliveryquesting.task.ItemReward;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class SealedEnvelopeItem extends Item {

    public SealedEnvelopeItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(MailboxParcel parcel) {
        if (!parcel.isContractEnvelope()) {
            throw new IllegalArgumentException("Mailbox parcel does not contain a contract");
        }

        ItemStack stack = new ItemStack(ModItems.SEALED_ENVELOPE.get());

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

        Identifier taskId = parcel.contractTaskId().orElse(null);

        if (taskId == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack contract = ContractItem.create(taskId);

        player.getInventory().add(contract);

        if (!contract.isEmpty()) {
            player.drop(contract, false, false);
        }

        for (ItemReward reward : parcel.items()) {
            SealedParcelItem.giveReward(player, reward);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        MailboxParcel parcel = stack.get(ModDataComponents.MAILBOX_PARCEL.get());

        if (parcel == null) {
            return;
        }

        builder.accept(Component.translatable("tooltip.delivery_questing.sealed_envelope.sender", parcel.sender()).withStyle(ChatFormatting.DARK_BLUE));
        builder.accept(Component.translatable("tooltip.delivery_questing.sealed_envelope.contract").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.delivery_questing.sealed_envelope.open").withStyle(ChatFormatting.DARK_GRAY));
    }
}