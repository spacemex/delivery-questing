package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.item.ContractItem;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class BulletinBoardBlock extends Block {

    public BulletinBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BulletinBoardMenu.open(serverPlayer, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NonNull InteractionResult useItemOn(ItemStack stack, @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof ContractItem)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        Identifier taskId = ContractItem.getTaskId(stack);

        if (taskId == null) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(serverPlayer.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(serverPlayer.getUUID());

        if (optionalGroup.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("You must be in a delivery group to accept a contract."));
            return InteractionResult.SUCCESS_SERVER;
        }

        DeliveryGroup group = optionalGroup.get();
        Optional<TaskDefinition> optionalTask = TaskManager.getTask(taskId);

        if (optionalTask.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("That contract no longer exists."));
            return InteractionResult.SUCCESS_SERVER;
        }

        TaskRuntimeManager.ActionResult result = TaskRuntimeManager.acceptTask(data, group, optionalTask.get());

        if (result.success()) {
            data.clearOutstandingPhysicalContract(group.id(), taskId);
        }

        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }

        serverPlayer.sendSystemMessage(Component.literal(result.message()));
        return InteractionResult.SUCCESS_SERVER;
    }
}
