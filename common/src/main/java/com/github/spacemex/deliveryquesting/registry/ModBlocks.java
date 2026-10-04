package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.BulletinBoardBlock;
import com.github.spacemex.deliveryquesting.block.CardboardBoxBlock;
import com.github.spacemex.deliveryquesting.block.ComputerBlock;
import com.github.spacemex.deliveryquesting.block.MailboxBlock;
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
    public static final RegistrySupplier<MailboxBlock> MAILBOX;
    public static final RegistrySupplier<ComputerBlock> COMPUTER;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_1;

    public static void initialize() {
        BLOCKS.register();
    }

    private static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    static {
        BULLETIN_BOARD = BLOCKS.register("bulletin_board", () ->
                new BulletinBoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.f, 3.f)
                        .setId(key("bulletin_board"))));
        MAILBOX = BLOCKS.register("mailbox", () ->
                new MailboxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.f, 6.f)
                        .setId(key("mailbox"))));
        COMPUTER = BLOCKS.register("computer", () ->
                new ComputerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(1.5f, 3.f)
                        .setId(key("computer"))));
        CARDBOARD_BOX_TIER_1 = BLOCKS.register("cardboard_box_tier_1", () ->
                new CardboardBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL).strength(0.5f)
                        .setId(key("cardboard_box_tier_1"))));
    }
}
