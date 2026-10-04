package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.ComputerBlockEntity;
import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class ComputerBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING;
    public static final BooleanProperty ON;
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Shapes.or(
                    Block.box(5D, 0D, 2D, 12D, 1D, 5D),
                    Block.box(2D, 0D, 10D, 10D, 1D, 13D),
                    Block.box(0D, 4D, 9D, 1D, 12D, 11D),
                    Block.box(11D, 4D, 9D, 12D, 12D, 11D),
                    Block.box(5D, 0D, 9D, 7D, 4D, 10D),
                    Block.box(1D, 11D, 9D, 11D, 12D, 11D),
                    Block.box(1D, 4D, 9D, 11D, 5D, 11D),
                    Block.box(1D, 5D, 10D, 11D, 11D, 11D),
                    Block.box(1D, 0D, 2D, 3D, 1D, 5D),
                    Block.box(12D, 0D, 4D, 16D, 10D, 16D)
            ),
            Direction.SOUTH, Shapes.or(
                    Block.box(4D, 0D, 11D, 11D, 1D, 14D),
                    Block.box(6D, 0D, 3D, 14D, 1D, 6D),
                    Block.box(15D, 4D, 5D, 16D, 12D, 7D),
                    Block.box(4D, 4D, 5D, 5D, 12D, 7D),
                    Block.box(9D, 0D, 6D, 11D, 4D, 7D),
                    Block.box(5D, 11D, 5D, 15D, 12D, 7D),
                    Block.box(5D, 4D, 5D, 15D, 5D, 7D),
                    Block.box(5D, 5D, 5D, 15D, 11D, 6D),
                    Block.box(13D, 0D, 11D, 15D, 1D, 14D),
                    Block.box(0D, 0D, 0D, 4D, 10D, 12D)
            ),
            Direction.WEST, Shapes.or(
                    Block.box(2D, 0D, 4D, 5D, 1D, 11D),
                    Block.box(10D, 0D, 6D, 13D, 1D, 14D),
                    Block.box(9D, 4D, 15D, 11D, 12D, 16D),
                    Block.box(9D, 4D, 4D, 11D, 12D, 5D),
                    Block.box(9D, 0D, 9D, 10D, 4D, 11D),
                    Block.box(9D, 11D, 5D, 11D, 12D, 15D),
                    Block.box(9D, 4D, 5D, 11D, 5D, 15D),
                    Block.box(10D, 5D, 5D, 11D, 11D, 15D),
                    Block.box(2D, 0D, 13D, 5D, 1D, 15D),
                    Block.box(4D, 0D, 0D, 16D, 10D, 4D)
            ),
            Direction.EAST, Shapes.or(
                    Block.box(11D, 0D, 5D, 14D, 1D, 12D),
                    Block.box(3D, 0D, 2D, 6D, 1D, 10D),
                    Block.box(5D, 4D, 0D, 7D, 12D, 1D),
                    Block.box(5D, 4D, 11D, 7D, 12D, 12D),
                    Block.box(6D, 0D, 5D, 7D, 4D, 7D),
                    Block.box(5D, 11D, 1D, 7D, 12D, 11D),
                    Block.box(5D, 4D, 1D, 7D, 5D, 11D),
                    Block.box(5D, 5D, 1D, 6D, 11D, 11D),
                    Block.box(11D, 0D, 1D, 14D, 1D, 3D),
                    Block.box(0D, 0D, 12D, 12D, 10D, 16D)
            )
    );

    public ComputerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ON, false));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ComputerBlock::new);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) {
            return;
        }

        if (!(placer instanceof ServerPlayer player)) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof ComputerBlockEntity computer)) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        data.getGroupForPlayer(player.getUUID()).ifPresent(group -> computer.bindToGroup(group.id()));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ComputerBlockEntity computer) {
            ComputerMenu.open(serverPlayer, pos, computer);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NonNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NonNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ON);
    }

    @Override
    protected @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.NORTH));
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        ON = BooleanProperty.create("on");
    }
}