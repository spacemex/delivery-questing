package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.BulletinBoardBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<BulletinBoardBlock> BULLETIN_BOARD;

    public static void initialize() {
        BLOCKS.register();
    }

    private static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    static {
        BULLETIN_BOARD = BLOCKS.register("bulletin_board", () -> new BulletinBoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.f, 3.f)
                .setId(key("bulletin_board"))));
    }
}
