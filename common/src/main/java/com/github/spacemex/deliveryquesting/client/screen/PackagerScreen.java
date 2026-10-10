package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.PackagerBlockEntity;
import com.github.spacemex.deliveryquesting.menu.PackagerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class PackagerScreen extends AbstractContainerScreen<PackagerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/packager.png");
    private static final Identifier UPGRADE_SLOT_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/item/upgrade_slot.png");

    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 53;

    public PackagerScreen(PackagerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,
                256, 256);

        drawEnergyBar(graphics, leftPos + 30, topPos + 17, menu.energy(), PackagerBlockEntity.ENERGY_CAPACITY);

        if (!menu.getSlot(PackagerBlockEntity.UPGRADE_SLOT).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT_TEXTURE, leftPos + 7, topPos + 17, 0,
                    0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFF404040, false);

        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);
    }

    private void drawEnergyBar(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        if (capacity <= 0 || energy <= 0) {
            return;
        }

        int filled = Math.round(energy / (float) capacity * BAR_HEIGHT);

        filled = Math.max(0, Math.min(BAR_HEIGHT, filled));

        int missing = BAR_HEIGHT - filled;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + missing, 176, missing,
                BAR_WIDTH, filled, 256, 256);
    }
}