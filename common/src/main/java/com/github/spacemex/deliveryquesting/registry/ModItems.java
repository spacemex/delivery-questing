package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.item.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

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

    public static void initialize() {
        ITEMS.register();
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    static {
        BULLETIN_BOARD = ITEMS.register("bulletin_board", () -> new BlockItem(ModBlocks.BULLETIN_BOARD.get(),
                new Item.Properties().useBlockDescriptionPrefix().setId(key("bulletin_board"))));
        MAILBOX = ITEMS.register("mailbox", () -> new BlockItem(ModBlocks.MAILBOX.get(),
                new Item.Properties().useBlockDescriptionPrefix().setId(key("mailbox"))));
        SEALED_PARCEL = ITEMS.register("sealed_parcel", () -> new SealedParcelItem(new Item.Properties()
                .stacksTo(1).setId(key("sealed_parcel"))));
        COMPUTER = ITEMS.register("computer", () -> new BlockItem(ModBlocks.COMPUTER.get(),
                new Item.Properties().useBlockDescriptionPrefix().setId(key("computer"))));
        ENVELOPE = ITEMS.register("envelope", () -> new DeliveryContainerItem(
                new Item.Properties().stacksTo(1).setId(key("envelope")), 1));
        PARCEL = ITEMS.register("parcel", () -> new DeliveryContainerItem(
                new Item.Properties().stacksTo(1).setId(key("parcel")), 16));
        CONTRACT = ITEMS.register("contract", () -> new ContractItem(
                new Item.Properties().stacksTo(1).setId(key("contract"))));
        SEALED_ENVELOPE = ITEMS.register("sealed_envelope", () -> new SealedEnvelopeItem(
                new Item.Properties().stacksTo(1).setId(key("sealed_envelope"))));
        CARDBOARD_BOX_TIER_1 = ITEMS.register("cardboard_box_tier_1", () -> new CardboardBoxItem(
                ModBlocks.CARDBOARD_BOX_TIER_1.get(), new Item.Properties().stacksTo(1).useBlockDescriptionPrefix()
                .setId(key("cardboard_box_tier_1"))));
    }
}
