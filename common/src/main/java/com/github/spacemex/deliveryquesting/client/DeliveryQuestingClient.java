package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.client.screen.*;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import dev.architectury.registry.client.gui.MenuScreenRegistry;

public final class DeliveryQuestingClient {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        MenuScreenRegistry.registerScreenFactory(ModMenus.BULLETIN_BOARD.get(), BulletinBoardScreen::new);
        MenuScreenRegistry.registerScreenFactory(ModMenus.MAILBOX.get(), MailboxScreen::new);
        MenuScreenRegistry.registerScreenFactory(ModMenus.COMPUTER.get(), ComputerScreen::new);
        MenuScreenRegistry.registerScreenFactory(ModMenus.DELIVERY_CONTAINER.get(), DeliveryContainerScreen::new);
        MenuScreenRegistry.registerScreenFactory(ModMenus.CONTRACT.get(), ContractScreen::new);
    }
}
