package com.github.spacemex.deliveryquesting.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityTypeFactory {
    private BlockEntityTypeFactory() {}

    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> create(Factory<? extends T> factory, Block... blocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface Factory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}