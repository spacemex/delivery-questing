package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.*;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<BulletinBoardMenu>> BULLETIN_BOARD;
    public static final RegistrySupplier<MenuType<MailboxMenu>> MAILBOX;
    public static final RegistrySupplier<MenuType<ComputerMenu>> COMPUTER;
    public static final RegistrySupplier<MenuType<DeliveryContainerMenu>> DELIVERY_CONTAINER;
    public static final RegistrySupplier<MenuType<ContractMenu>> CONTRACT;
    public static final RegistrySupplier<MenuType<CardboardBoxMenu>> CARDBOARD_BOX_TIER_1;

    public static void initialize() {
        MENUS.register();
    }

    static {
        BULLETIN_BOARD = MENUS.register("bulletin_board", () -> MenuRegistry.ofExtended(BulletinBoardMenu::fromNetwork));
        MAILBOX = MENUS.register("mailbox", () -> MenuRegistry.ofExtended(MailboxMenu::fromNetwork));
        COMPUTER = MENUS.register("computer", () -> MenuRegistry.ofExtended(ComputerMenu::fromNetwork));
        DELIVERY_CONTAINER = MENUS.register("delivery_container", () -> MenuRegistry.ofExtended(DeliveryContainerMenu::fromNetwork));
        CONTRACT = MENUS.register("contract", () -> MenuRegistry.ofExtended(ContractMenu::fromNetwork));
        CARDBOARD_BOX_TIER_1 = MENUS.register("cardboard_box_tier_1", () -> MenuRegistry.ofExtended(CardboardBoxMenu::fromNetwork));
    }
}
