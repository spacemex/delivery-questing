package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.client.render.DroneRenderer;
import com.github.spacemex.deliveryquesting.client.screen.*;
import com.github.spacemex.deliveryquesting.registry.ModEntities;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

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
        MenuScreenRegistry.registerScreenFactory(ModMenus.CARDBOARD_BOX.get(), CardboardBoxScreen::new);
        MenuScreenRegistry.registerScreenFactory(ModMenus.DRONE_PAD.get(), DronePadScreen::new);

        EntityRendererRegistry.register(ModEntities.DRONE, DroneRenderer::new);
    }
}
