package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.CardboardBoxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class CardboardBoxScreen extends AbstractContainerScreen<CardboardBoxMenu> {
    public CardboardBoxScreen(CardboardBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFD8C19F);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF5C4630);
        graphics.fill(leftPos + 79, topPos + 34, leftPos + 97, topPos + 52, 0xFF8B6A48);
        graphics.fill(leftPos + 80, topPos + 35, leftPos + 96, topPos + 51, 0xFFC7A579);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 8, 0xFF322719, false);
        graphics.text(font, Component.literal("1 Stack"), 8, 22, 0xFF66513A, false);
        graphics.text(font, playerInventoryTitle, 8, 72, 0xFF66513A, false);
    }
}