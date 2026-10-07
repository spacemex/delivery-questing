package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.DeliveryContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class DeliveryContainerScreen extends AbstractContainerScreen<DeliveryContainerMenu> {

    private static final Identifier ENVELOPE_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/envelope.png");
    private static final Identifier PARCEL_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/parcel.png");

    public DeliveryContainerScreen(DeliveryContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 133);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        Identifier texture = menu.capacity() == 1 ? ENVELOPE_TEXTURE : PARCEL_TEXTURE;

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos,
                0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 9, 0xFF404040, false);

        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);
    }
}