package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.menu.ContractMenu;
import com.github.spacemex.deliveryquesting.registry.ModDataComponents;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public final class ContractItem extends Item {

    public ContractItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(Identifier taskId) {
        ItemStack stack = new ItemStack(ModItems.CONTRACT.get());

        stack.set(ModDataComponents.CONTRACT_TASK_ID.get(), taskId);

        return stack;
    }

    @Nullable
    public static Identifier getTaskId(ItemStack stack) {
        return stack.get(ModDataComponents.CONTRACT_TASK_ID.get());
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Identifier taskId = getTaskId(stack);

        if (taskId == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ContractMenu.open(serverPlayer, taskId);

        return InteractionResult.SUCCESS_SERVER;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        builder.accept(Component.translatable("tooltip.delivery_questing.contract.inspect")
                .withStyle(ChatFormatting.GRAY));

        Identifier taskId = getTaskId(stack);

        if (taskId != null) {
            builder.accept(Component.literal(taskId.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}