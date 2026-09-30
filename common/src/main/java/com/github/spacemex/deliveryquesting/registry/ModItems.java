package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
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

    public static void initialize() {
        ITEMS.register();
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    static {
        BULLETIN_BOARD = ITEMS.register("bulletin_board", ()-> new BlockItem(ModBlocks.BULLETIN_BOARD.get(),
                new Item.Properties().useBlockDescriptionPrefix().setId(key("bulletin_board"))));
    }
}
