package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.DeliveryContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class DeliveryContainerScreen extends AbstractContainerScreen<DeliveryContainerMenu> {

    public DeliveryContainerScreen(DeliveryContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFD7D7D7);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF454545);
        graphics.fill(leftPos + 79, topPos + 34, leftPos + 97, topPos + 52, 0xFF777777);
        graphics.fill(leftPos + 80, topPos + 35, leftPos + 96, topPos + 51, 0xFFB8B8B8);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 8, 0xFF222222, false);
        graphics.text(font, Component.literal("Capacity: " + menu.capacity()), 8, 22, 0xFF555555, false);
        graphics.text(font, playerInventoryTitle, 8, 72, 0xFF555555, false);
    }
}