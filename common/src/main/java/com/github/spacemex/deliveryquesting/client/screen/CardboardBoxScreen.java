package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.CardboardBoxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class CardboardBoxScreen extends AbstractContainerScreen<CardboardBoxMenu> {

    public CardboardBoxScreen(CardboardBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, menu.imageHeight());
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTexture(), leftPos, topPos,
                0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font,title,(imageWidth - font.width(title)) / 2, 9, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);
    }

    private Identifier getTexture() {
        return Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/cardboard_box_tier_"
                + menu.tier().level() + ".png");
    }
}