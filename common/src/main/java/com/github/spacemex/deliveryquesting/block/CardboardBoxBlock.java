package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.CardboardBoxBlockEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxTier;
import com.github.spacemex.deliveryquesting.menu.CardboardBoxMenu;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public final class CardboardBoxBlock extends BaseEntityBlock {
    public static final MapCodec<CardboardBoxBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CardboardBoxTier.CODEC.fieldOf("tier")
                    .forGetter(CardboardBoxBlock::tier), propertiesCodec()).apply(instance, CardboardBoxBlock::new));
    private final CardboardBoxTier tier;
    public static final EnumProperty<Direction> FACING;

    public CardboardBoxBlock(CardboardBoxTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    public CardboardBoxTier tier() {
        return tier;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new CardboardBoxBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof CardboardBoxBlockEntity box) {
            CardboardBoxMenu.open(serverPlayer, pos, box);
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
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
    }
}