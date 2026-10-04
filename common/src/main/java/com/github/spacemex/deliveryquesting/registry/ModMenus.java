package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.*;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<BulletinBoardMenu>> BULLETIN_BOARD;
    public static final RegistrySupplier<MenuType<MailboxMenu>> MAILBOX;
    public static final RegistrySupplier<MenuType<ComputerMenu>> COMPUTER;
    public static final RegistrySupplier<MenuType<DeliveryContainerMenu>> DELIVERY_CONTAINER;
    public static final RegistrySupplier<MenuType<ContractMenu>> CONTRACT;
    public static final RegistrySupplier<MenuType<CardboardBoxMenu>> CARDBOARD_BOX;
    public static final RegistrySupplier<MenuType<DronePadMenu>> DRONE_PAD;

    public static void initialize() {
        MENUS.register();
    }

    private static <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> register(String name, MenuRegistry.ExtendedMenuTypeFactory<T> factory) {
        return MENUS.register(name, () -> MenuRegistry.ofExtended(factory));
    }

    static {
        BULLETIN_BOARD = register("bulletin_board", BulletinBoardMenu::fromNetwork);
        MAILBOX = register("mailbox", MailboxMenu::fromNetwork);
        COMPUTER = register("computer", ComputerMenu::fromNetwork);
        DELIVERY_CONTAINER = register("delivery_container", DeliveryContainerMenu::fromNetwork);
        CONTRACT = register("contract", ContractMenu::fromNetwork);
        CARDBOARD_BOX = register("cardboard_box", CardboardBoxMenu::fromNetwork);
        DRONE_PAD = register("drone_pad", DronePadMenu::fromNetwork);
    }
}
