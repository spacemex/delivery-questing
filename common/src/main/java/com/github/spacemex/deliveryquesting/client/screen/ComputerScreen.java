package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {

    public ComputerScreen(ComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 300, 190);
    }

    @Override
    protected void init() {
        super.init();

        Button mail = this.addRenderableWidget(Button.builder(
                        Component.literal("Mail"), ignored -> {
                        })
                .bounds(leftPos + 25, topPos + 118, 110, 24).build());

        mail.active = false;

        Button minazon = this.addRenderableWidget(
                Button.builder(Component.literal("Minazon"), ignored -> {
                        })
                        .bounds(leftPos + 165, topPos + 118, 110, 24).build());

        minazon.active = false;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF1B1E23);
        graphics.fill(leftPos + 8, topPos + 8, leftPos + imageWidth - 8, topPos + imageHeight - 8, 0xFF263238);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF707A80);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal(menu.groupName()), 14, 33, 0xFF80CBC4, false);
        graphics.text(font, Component.literal("Delivery Level: " + menu.level()), 14, 54, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Balance: " + menu.balance()), 14, 68, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Active Contracts: " + menu.activeTasks()), 14, 82, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Completed Contracts: " + menu.completedTasks()), 14, 96, 0xFFD7E1E5, false);
        graphics.centeredText(font, Component.literal("Applications coming online..."), imageWidth / 2, 156, 0xFF9EA7AA);
    }
}