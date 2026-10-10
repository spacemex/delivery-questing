package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.BarrelBlock;
import com.github.spacemex.deliveryquesting.block.CardboardBoxBlock;
import com.github.spacemex.deliveryquesting.item.*;
import com.github.spacemex.deliveryquesting.item.tier.BarrelTier;
import com.github.spacemex.deliveryquesting.item.tier.CardboardBoxTier;
import com.github.spacemex.deliveryquesting.item.tier.UpgradeTier;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {

    private ModItems() {
    }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.ITEM);

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
    public static final RegistrySupplier<Item> UPGRADE_BASE;
    public static final RegistrySupplier<BlockItem> DRONE_PAD;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_1;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_2;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_3;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_4;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_5;
    public static final RegistrySupplier<UpgradeItem> UPGRADE_TIER_6;
    public static final RegistrySupplier<BlockItem> PACKAGER;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_1;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_2;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_3;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_4;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_5;
    public static final RegistrySupplier<BarrelItem> BARREL_TIER_6;

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

    private static RegistrySupplier<BarrelItem> register(BarrelTier tier, RegistrySupplier<BarrelBlock> block) {
        String name = "barrel_tier_" + tier.level();
        return register(name, p -> {
            p.stacksTo(1);
            p.useBlockDescriptionPrefix();
            return p;
        }, p -> new BarrelItem(block.get(), tier, p));
    }

    private static RegistrySupplier<UpgradeItem> register(UpgradeTier tier) {
        String name = "upgrade_tier_" + tier.level();
        return register(name, p -> new UpgradeItem(tier, p));
    }

    private static RegistrySupplier<BlockItem> register(String name, RegistrySupplier<? extends Block> block) {
        return register(name, Item.Properties::useBlockDescriptionPrefix, p -> new BlockItem(block.get(), p));
    }

    private static RegistrySupplier<DeliveryContainerItem> register(String name, int capacity) {
        return register(name, p -> p.stacksTo(1), p -> new DeliveryContainerItem(p, capacity));
    }

    @ApiStatus.Experimental
    @SuppressWarnings("all")
    private static <T extends Item> RegistrySupplier<T> register(String name, UnaryOperator<Item.Properties> properties, Function<Item.Properties, T> factory) {
        return ITEMS.register(name, () -> factory.apply(properties.apply(new Item.Properties()).arch$tab(ModCreativeModeTabs.DELIVERY_TAB).setId(key(name))));
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
        DRONE_PAD = register("drone_pad", ModBlocks.DRONE_PAD);
        UPGRADE_BASE = register("upgrade_base", Item::new);
        UPGRADE_TIER_1 = register(UpgradeTier.TIER_1);
        UPGRADE_TIER_2 = register(UpgradeTier.TIER_2);
        UPGRADE_TIER_3 = register(UpgradeTier.TIER_3);
        UPGRADE_TIER_4 = register(UpgradeTier.TIER_4);
        UPGRADE_TIER_5 = register(UpgradeTier.TIER_5);
        UPGRADE_TIER_6 = register(UpgradeTier.TIER_6);
        PACKAGER = register("packager", ModBlocks.PACKAGER);
        BARREL_TIER_1 = register(BarrelTier.TIER_1, ModBlocks.BARREL_BLOCK_TIER_1);
        BARREL_TIER_2 = register(BarrelTier.TIER_2, ModBlocks.BARREL_BLOCK_TIER_2);
        BARREL_TIER_3 = register(BarrelTier.TIER_3, ModBlocks.BARREL_BLOCK_TIER_3);
        BARREL_TIER_4 = register(BarrelTier.TIER_4, ModBlocks.BARREL_BLOCK_TIER_4);
        BARREL_TIER_5 = register(BarrelTier.TIER_5, ModBlocks.BARREL_BLOCK_TIER_5);
        BARREL_TIER_6 = register(BarrelTier.TIER_6, ModBlocks.BARREL_BLOCK_TIER_6);
    }
}
