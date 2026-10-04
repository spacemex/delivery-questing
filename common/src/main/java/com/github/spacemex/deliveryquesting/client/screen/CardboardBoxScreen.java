package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.CardboardBoxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class CardboardBoxScreen extends AbstractContainerScreen<CardboardBoxMenu> {

    public CardboardBoxScreen(CardboardBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, menu.imageHeight());
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFD8C19F);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF5C4630);

        int startX = menu.boxSlotStartX();
        int startY = menu.boxSlotStartY();

        for (int i = 0; i < menu.tier().slots(); i++) {
            int column = i % menu.tier().columns();
            int row = i / menu.tier().columns();
            int x = leftPos + startX + column * 18;
            int y = topPos + startY + row * 18;

            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B6A48);
            graphics.fill(x, y, x + 16, y + 16, 0xFFC7A579);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 6, 0xFF322719, false);
        graphics.text(font, Component.literal(menu.tier().slots() + " Stack" + (menu.tier().slots() == 1 ? "" : "s")), 8,
                menu.playerInventoryY() - 14, 0xFF66513A, false);
        graphics.text(font, playerInventoryTitle, 8, menu.playerInventoryY() - 10, 0xFF66513A, false);
    }
}