package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.client.screen.BulletinBoardScreen;
import com.github.spacemex.deliveryquesting.client.screen.MailboxScreen;
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
    }
}
