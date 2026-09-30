package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<BulletinBoardMenu>> BULLETIN_BOARD;

    public static void initialize() {
        MENUS.register();
    }

    static {
        BULLETIN_BOARD = MENUS.register("bulletin_board", () -> MenuRegistry.ofExtended(BulletinBoardMenu::fromNetwork));
    }
}
