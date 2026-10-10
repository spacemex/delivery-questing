
package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.BarrelBlockEntity;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.item.tier.BarrelTier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public final class BarrelBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public static final MapCodec<BarrelBlock> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BarrelTier.CODEC.fieldOf("tier")
                            .forGetter(BarrelBlock::tier), propertiesCodec()).apply(instance, BarrelBlock::new));

    private final BarrelTier tier;

    public BarrelBlock(BarrelTier tier, Properties properties) {
        super(properties);
        this.tier = tier;

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    public BarrelTier tier() {
        return tier;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack stack, @NonNull BlockState state,
                                                   @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player,
                                                   @NonNull InteractionHand hand, @NonNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        boolean water = stack.is(Items.WATER_BUCKET);
        boolean lava = stack.is(Items.LAVA_BUCKET);
        boolean emptyBucket = stack.is(Items.BUCKET);

        if (!water && !lava && !emptyBucket) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (water || lava) {
            var fluid = water ? BarrelContents.WATER : BarrelContents.LAVA;

            if (barrel.fill(fluid, 1_000, true) != 1_000) {
                return InteractionResult.FAIL;
            }

            barrel.fill(fluid, 1_000, false);

            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        BarrelContents contents = barrel.getContents();

        if (contents.amount() < 1_000) {
            return InteractionResult.FAIL;
        }

        ItemStack filledBucket;

        if (contents.fluid().equals(BarrelContents.WATER)) {
            filledBucket = new ItemStack(Items.WATER_BUCKET);
        } else if (contents.fluid().equals(BarrelContents.LAVA)) {
            filledBucket = new ItemStack(Items.LAVA_BUCKET);
        } else {
            return InteractionResult.FAIL;
        }

        barrel.drain(1_000, false);

        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, filledBucket);
        }

        return InteractionResult.SUCCESS_SERVER;
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
}
