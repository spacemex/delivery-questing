package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.item.ContractItem;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import com.github.spacemex.deliveryquesting.task.definition.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.manager.TaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;

public final class BulletinBoardBlock extends Block {

    public static final EnumProperty<Direction> FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Shapes.or(
                    Block.box(0D, 0D, 15D, 16D, 16D, 16D),
                    Block.box(0D, 0D, 14D, 1D, 16D, 15D),
                    Block.box(15D, 0D, 14D, 16D, 16D, 15D),
                    Block.box(1D, 15D, 14D, 15D, 16D, 15D),
                    Block.box(1D, 0D, 14D, 15D, 1D, 15D)
            ),
            Direction.SOUTH, Shapes.or(
                    Block.box(0D, 0D, 0D, 16D, 16D, 1D),
                    Block.box(15D, 0D, 1D, 16D, 16D, 2D),
                    Block.box(0D, 0D, 1D, 1D, 16D, 2D),
                    Block.box(1D, 15D, 1D, 15D, 16D, 2D),
                    Block.box(1D, 0D, 1D, 15D, 1D, 2D)
            ),
            Direction.WEST, Shapes.or(
                    Block.box(15D, 0D, 0D, 16D, 16D, 16D),
                    Block.box(14D, 0D, 15D, 15D, 16D, 16D),
                    Block.box(14D, 0D, 0D, 15D, 16D, 1D),
                    Block.box(14D, 15D, 1D, 15D, 16D, 15D),
                    Block.box(14D, 0D, 1D, 15D, 1D, 15D)
            ),
            Direction.EAST, Shapes.or(
                    Block.box(0D, 0D, 0D, 1D, 16D, 16D),
                    Block.box(1D, 0D, 0D, 2D, 16D, 1D),
                    Block.box(1D, 0D, 15D, 2D, 16D, 16D),
                    Block.box(1D, 15D, 1D, 2D, 16D, 15D),
                    Block.box(1D, 0D, 1D, 2D, 1D, 15D)
            )
    );

    public BulletinBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @NonNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NonNull BlockState rotate(@NonNull BlockState state, @NonNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NonNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level,
                                                        @NonNull BlockPos pos, @NonNull Player player,
                                                        @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BulletinBoardMenu.open(serverPlayer, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(FACING);
    }

    @Override
    protected @NonNull InteractionResult useItemOn(ItemStack stack, @NonNull BlockState state,
                                                   @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player,
                                                   @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
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
            serverPlayer.sendSystemMessage(
                    Component.literal("You must be in a delivery group to accept a contract."));

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

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter level,
                                           @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.NORTH));
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
    }
}
