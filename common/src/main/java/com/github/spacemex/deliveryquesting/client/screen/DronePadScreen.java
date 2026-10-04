package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.DronePadMenu;
import com.github.spacemex.deliveryquesting.networking.packets.SubmitDroneDeliveryPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class DronePadScreen extends AbstractContainerScreen<DronePadMenu> {
    private Button sendButton;

    public DronePadScreen(DronePadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        sendButton = addRenderableWidget(Button.builder(Component.literal("Send Delivery"), button -> {
            button.active = false;
            NetworkManager.sendToServer(new SubmitDroneDeliveryPayload(menu.blockPos()));
        }).bounds(leftPos + 50, topPos + 57, 76, 20).build());
        sendButton.active = menu.hasPayload();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (sendButton != null) {
            sendButton.active = menu.hasPayload();
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFB8B8B8);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF353535);
        graphics.fill(leftPos + 79, topPos + 34, leftPos + 97, topPos + 52, 0xFF555555);
        graphics.fill(leftPos + 80, topPos + 35, leftPos + 96, topPos + 51, 0xFF909090);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 8, 0xFF202020, false);
        graphics.text(font, Component.literal("Cardboard Box Payload"), 8, 22, 0xFF454545, false);
        graphics.text(font, playerInventoryTitle, 8, 72, 0xFF454545, false);
    }
}