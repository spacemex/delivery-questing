
package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.BarrelBlockEntity;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.item.tier.BarrelTier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack stack, @NonNull BlockState state, @NonNull Level level,
                                                   @NonNull BlockPos pos, @NonNull Player player,
                                                   @NonNull InteractionHand hand, @NonNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        boolean emptyBucket = stack.is(Items.BUCKET);

        boolean filledBucket = stack.getItem() instanceof BucketItem
                && !emptyBucket && !(stack.getItem() instanceof MobBucketItem);

        if (!emptyBucket && !filledBucket) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (filledBucket) {
            BucketItem bucket = (BucketItem) stack.getItem();

            Fluid fluid = bucket.getContent();

            if (fluid instanceof FlowingFluid flowing) {
                fluid = flowing.getSource();
            }

            if (fluid == Fluids.EMPTY) {
                return InteractionResult.FAIL;
            }

            Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);

            if (barrel.fill(fluidId, 1_000, true) != 1_000) {
                return InteractionResult.FAIL;
            }

            barrel.fill(fluidId, 1_000, false);

            player.setItemInHand(hand, BucketItem.getEmptySuccessItem(stack, player));

            return InteractionResult.SUCCESS_SERVER;
        }

        BarrelContents contents = barrel.getContents();

        if (contents.amount() < 1_000) {
            return InteractionResult.FAIL;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(contents.fluid());

        if (fluid == null || fluid == Fluids.EMPTY) {
            return InteractionResult.FAIL;
        }

        if (fluid instanceof FlowingFluid flowing) {
            fluid = flowing.getSource();
        }

        Item bucketItem = fluid.getBucket();

        if (!(bucketItem instanceof BucketItem) || bucketItem == Items.BUCKET) {
            return InteractionResult.FAIL;
        }

        barrel.drain(1_000, false);

        ItemStack filled = new ItemStack(bucketItem);

        if (player.getAbilities().instabuild) {
            player.setItemInHand(hand, filled);
        } else {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
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
