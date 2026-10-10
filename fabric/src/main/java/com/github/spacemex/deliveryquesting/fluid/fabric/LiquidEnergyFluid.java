package com.github.spacemex.deliveryquesting.fluid.fabric;

import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

@SuppressWarnings("all")
public abstract class LiquidEnergyFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.LIQUID_ENERGY_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.LIQUID_ENERGY.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource()
                || fluid == getFlowing();
    }

    @Override
    public Item getBucket() {
        return ModItems.LIQUID_ENERGY_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level lvl) {
            Block.dropResources(state, lvl, pos);
        }
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    public int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(
            FluidState state,
            BlockGetter level,
            BlockPos pos,
            Fluid fluid,
            Direction direction
    ) {
        return direction == Direction.DOWN
                && !isSame(fluid);
    }

    @Override
    protected float getExplosionResistance() {
        return 100F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.LIQUID_ENERGY.get()
                .defaultBlockState()
                .setValue(
                        LiquidBlock.LEVEL,
                        getLegacyLevel(state)
                );
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    public static final class Source extends LiquidEnergyFluid {

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends LiquidEnergyFluid {

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder
        ) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}