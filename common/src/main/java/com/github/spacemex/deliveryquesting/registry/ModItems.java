package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.CardboardBoxBlock;
import com.github.spacemex.deliveryquesting.item.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<BlockItem> BULLETIN_BOARD;
    public static final RegistrySupplier<BlockItem> MAILBOX;
    public static final RegistrySupplier<BlockItem> COMPUTER;
    public static final RegistrySupplier<SealedParcelItem> SEALED_PARCEL;
    public static final RegistrySupplier<DeliveryContainerItem> ENVELOPE;
    public static final RegistrySupplier<DeliveryContainerItem> PARCEL;
    public static final RegistrySupplier<ContractItem> CONTRACT;
    public static final RegistrySupplier<SealedEnvelopeItem> SEALED_ENVELOPE;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_1;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_2;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_3;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_4;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_5;
    public static final RegistrySupplier<CardboardBoxItem> CARDBOARD_BOX_TIER_6;
    public static final RegistrySupplier<Item> CARDBOARD;

    public static void initialize() {
        ITEMS.register();
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    private static RegistrySupplier<CardboardBoxItem> register(CardboardBoxTier tier, RegistrySupplier<CardboardBoxBlock> block) {
        String name = "cardboard_box_tier_" + tier.level();
        return register(name, p -> {
            p.stacksTo(1);
            p.useBlockDescriptionPrefix();
            return p;
        }, p -> new CardboardBoxItem(block.get(), tier, p));
    }

    private static RegistrySupplier<BlockItem> register(String name, RegistrySupplier<? extends Block> block) {
        return register(name, Item.Properties::useBlockDescriptionPrefix, p -> new BlockItem(block.get(), p));
    }

    private static RegistrySupplier<DeliveryContainerItem> register(String name, int capacity) {
        return register(name, p -> p.stacksTo(1), p -> new DeliveryContainerItem(p, capacity));
    }

    private static <T extends Item> RegistrySupplier<T> register(String name, UnaryOperator<Item.Properties> properties, Function<Item.Properties, T> factory) {
        return ITEMS.register(name, () -> factory.apply(properties.apply(new Item.Properties()).setId(key(name))));
    }

    private static <T extends Item> RegistrySupplier<T> register(String name, Function<Item.Properties, T> factory) {
        return register(name, UnaryOperator.identity(), factory);
    }


    static {
        BULLETIN_BOARD = register("bulletin_board", ModBlocks.BULLETIN_BOARD);
        MAILBOX = register("mailbox", ModBlocks.MAILBOX);
        SEALED_PARCEL = register("sealed_parcel", p -> p.stacksTo(1), SealedParcelItem::new);
        COMPUTER = register("computer", ModBlocks.COMPUTER);
        ENVELOPE = register("envelope", 1);
        PARCEL = register("parcel", 16);
        CONTRACT = register("contract", p -> p.stacksTo(1), ContractItem::new);
        SEALED_ENVELOPE = register("sealed_envelope", p -> p.stacksTo(1), SealedEnvelopeItem::new);
        CARDBOARD_BOX_TIER_1 = register(CardboardBoxTier.TIER_1, ModBlocks.CARDBOARD_BOX_TIER_1);
        CARDBOARD_BOX_TIER_2 = register(CardboardBoxTier.TIER_2, ModBlocks.CARDBOARD_BOX_TIER_2);
        CARDBOARD_BOX_TIER_3 = register(CardboardBoxTier.TIER_3, ModBlocks.CARDBOARD_BOX_TIER_3);
        CARDBOARD_BOX_TIER_4 = register(CardboardBoxTier.TIER_4, ModBlocks.CARDBOARD_BOX_TIER_4);
        CARDBOARD_BOX_TIER_5 = register(CardboardBoxTier.TIER_5, ModBlocks.CARDBOARD_BOX_TIER_5);
        CARDBOARD_BOX_TIER_6 = register(CardboardBoxTier.TIER_6, ModBlocks.CARDBOARD_BOX_TIER_6);
        CARDBOARD = register("cardboard", Item::new);
    }
}
