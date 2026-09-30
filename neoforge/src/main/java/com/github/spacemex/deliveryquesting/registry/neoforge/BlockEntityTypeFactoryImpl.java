package com.github.spacemex.deliveryquesting.registry.neoforge;

import com.github.spacemex.deliveryquesting.registry.BlockEntityTypeFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityTypeFactoryImpl {

    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityTypeFactory.Factory<? extends T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::create, blocks);
    }
}