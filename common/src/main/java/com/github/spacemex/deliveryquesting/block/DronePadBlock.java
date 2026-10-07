package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.menu.DronePadMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DronePadBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Shapes.or(
                    Block.box(0D, 0D, 0D, 16D, 1D, 16D),
                    Block.box(1D, 1D, 1D, 15D, 2D, 15D),
                    Block.box(1D, 1D, 15D, 15D, 12D, 16D),
                    Block.box(4D, 2D, 14D, 12D, 9D, 15D)
            ),
            Direction.SOUTH, Shapes.or(
                    Block.box(0D, 0D, 0D, 16D, 1D, 16D),
                    Block.box(1D, 1D, 1D, 15D, 2D, 15D),
                    Block.box(1D, 1D, 0D, 15D, 12D, 1D),
                    Block.box(4D, 2D, 1D, 12D, 9D, 2D)
            ),
            Direction.WEST, Shapes.or(
                    Block.box(0D, 0D, 0D, 16D, 1D, 16D),
                    Block.box(1D, 1D, 1D, 15D, 2D, 15D),
                    Block.box(15D, 1D, 1D, 16D, 12D, 15D),
                    Block.box(14D, 2D, 4D, 15D, 9D, 12D)
            ),
            Direction.EAST, Shapes.or(
                    Block.box(0D, 0D, 0D, 16D, 1D, 16D),
                    Block.box(1D, 1D, 1D, 15D, 2D, 15D),
                    Block.box(0D, 1D, 1D, 1D, 12D, 15D),
                    Block.box(1D, 2D, 4D, 2D, 9D, 12D)
            )
    );

    public DronePadBlock(Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DronePadBlock::new);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new DronePadBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state,
                            @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide() || !(placer instanceof ServerPlayer player) || !(level.getBlockEntity(pos)
                instanceof DronePadBlockEntity pad)) {
            return;
        }
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());

        data.getGroupForPlayer(player.getUUID()).ifPresent(group -> pad.bindToGroup(group.id()));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level,
                                                        @NonNull BlockPos pos, @NonNull Player player,
                                                        @NonNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(level.getBlockEntity(pos)
                instanceof DronePadBlockEntity pad)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(serverPlayer.level().getServer());

        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(serverPlayer.getUUID());

        if (optionalGroup.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.no_group"));

            return InteractionResult.SUCCESS_SERVER;
        }

        DeliveryGroup group = optionalGroup.get();

        Optional<UUID> boundGroup = pad.groupId();

        if (boundGroup.isEmpty()) {
            pad.bindToGroup(group.id());
        } else if (!boundGroup.get().equals(group.id())) {
            if (data.getGroup(boundGroup.get()).isPresent()) {

                serverPlayer.sendSystemMessage(
                        Component.translatable("message.delivery_questing.drone_pad.wrong_group"));

                return InteractionResult.SUCCESS_SERVER;
            }

            pad.rebindToGroup(group.id());

            serverPlayer.sendSystemMessage(Component.literal("Reclaimed abandoned Drone Pad for '"
                    + group.name() + "'."));
        }

        if (!pad.isSkyFree()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.no_sky"));

            return InteractionResult.SUCCESS_SERVER;
        }
        DronePadMenu.open(serverPlayer, pos, pad);

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.DRONE_PAD.get(), DronePadBlockEntity::tick);
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
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(FACING);
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