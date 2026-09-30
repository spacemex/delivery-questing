package com.github.spacemex.deliveryquesting.registry.fabric;

import com.github.spacemex.deliveryquesting.registry.BlockEntityTypeFactory;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityTypeFactoryImpl {

    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityTypeFactory.Factory<? extends T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.<T>create(factory::create, blocks).build();
    }
}