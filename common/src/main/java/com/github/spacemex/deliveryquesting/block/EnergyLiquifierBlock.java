
package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public final class EnergyLiquifierBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING =
            HorizontalDirectionalBlock.FACING;

    public EnergyLiquifierBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(EnergyLiquifierBlock::new);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(
            @NonNull BlockPos pos, @NonNull BlockState state) {
        return new EnergyLiquifierBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hit) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof EnergyLiquifierBlockEntity machine)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            machine.toggleReversed();
        }

        String mode = machine.isReversed()
                ? "Liquid Energy -> FE"
                : "FE -> Liquid Energy";

        player.sendSystemMessage(Component.literal(
                "Energy Liquifier [" + mode + "] "
                        + machine.getEnergy() + "/" + machine.getMaxEnergy()
                        + " FE | "
                        + machine.getFluidAmount() + "/"
                        + machine.getFluidCapacity() + " mB"));

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> type) {

        return createTickerHelper(
                type,
                ModBlockEntities.ENERGY_LIQUIFIER.get(),
                EnergyLiquifierBlockEntity::tick);
    }

    @Override
    public @NonNull BlockState getStateForPlacement(
            BlockPlaceContext context) {

        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NonNull BlockState rotate(
            BlockState state, Rotation rotation) {

        return state.setValue(
                FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NonNull BlockState mirror(
            BlockState state, Mirror mirror) {

        return state.rotate(
                mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
