package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.*;
import com.github.spacemex.deliveryquesting.item.tier.BarrelTier;
import com.github.spacemex.deliveryquesting.item.tier.CardboardBoxTier;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModBlocks {

    private ModBlocks() {
    }

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<BulletinBoardBlock> BULLETIN_BOARD;
    public static final RegistrySupplier<MailboxBlock> MAILBOX;
    public static final RegistrySupplier<ComputerBlock> COMPUTER;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_1;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_2;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_3;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_4;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_5;
    public static final RegistrySupplier<CardboardBoxBlock> CARDBOARD_BOX_TIER_6;
    public static final RegistrySupplier<DronePadBlock> DRONE_PAD;
    public static final RegistrySupplier<PackagerBlock> PACKAGER;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_1;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_2;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_3;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_4;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_5;
    public static final RegistrySupplier<BarrelBlock> BARREL_BLOCK_TIER_6;

    public static void initialize() {
        BLOCKS.register();
    }

    private static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    private static RegistrySupplier<CardboardBoxBlock> register(CardboardBoxTier tier) {
        String name = "cardboard_box_tier_" + tier.level();
        return register(name, BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL), p -> {
            p.strength(0.5f);
            return p;
        }, p -> new CardboardBoxBlock(tier, p));
    }

    private static RegistrySupplier<BarrelBlock> register(BarrelTier tier) {
        String name = "barrel_tier_" + tier.level();
        return register(name, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), p -> {
            p.strength(1.5f).noOcclusion();
            return p;
        }, p -> new BarrelBlock(tier, p));
    }

    private static <T extends Block> RegistrySupplier<T> register(String name, BlockBehaviour.Properties properties, UnaryOperator<BlockBehaviour.Properties> propertiesModifier, Function<BlockBehaviour.Properties, T> factory) {
        return BLOCKS.register(name, () -> factory.apply(propertiesModifier.apply(properties).setId(key(name))));
    }

    static {
        BULLETIN_BOARD = register("bulletin_board", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS), p -> p.strength(2.f, 3.f).noOcclusion(), BulletinBoardBlock::new);
        MAILBOX = register("mailbox", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), p -> p.strength(2.f, 6.f).noOcclusion(), MailboxBlock::new);
        COMPUTER = register("computer", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), p -> p.strength(1.5f, 3.f).noOcclusion(), ComputerBlock::new);
        CARDBOARD_BOX_TIER_1 = register(CardboardBoxTier.TIER_1);
        CARDBOARD_BOX_TIER_2 = register(CardboardBoxTier.TIER_2);
        CARDBOARD_BOX_TIER_3 = register(CardboardBoxTier.TIER_3);
        CARDBOARD_BOX_TIER_4 = register(CardboardBoxTier.TIER_4);
        CARDBOARD_BOX_TIER_5 = register(CardboardBoxTier.TIER_5);
        CARDBOARD_BOX_TIER_6 = register(CardboardBoxTier.TIER_6);
        DRONE_PAD = register("drone_pad", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), p -> p.strength(1.5f, 6.f).noOcclusion(), DronePadBlock::new);
        PACKAGER = register("packager", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), p -> p.strength(3.f, 6.f).noOcclusion(), PackagerBlock::new);
        BARREL_BLOCK_TIER_1 = register(BarrelTier.TIER_1);
        BARREL_BLOCK_TIER_2 = register(BarrelTier.TIER_2);
        BARREL_BLOCK_TIER_3 = register(BarrelTier.TIER_3);
        BARREL_BLOCK_TIER_4 = register(BarrelTier.TIER_4);
        BARREL_BLOCK_TIER_5 = register(BarrelTier.TIER_5);
        BARREL_BLOCK_TIER_6 = register(BarrelTier.TIER_6);
    }
}
