package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.widget.TaskRequirementWidget;
import com.github.spacemex.deliveryquesting.menu.ContractMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class ContractScreen extends AbstractContainerScreen<ContractMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/contract.png");
    private TaskRequirementWidget requirementWidget;

    public ContractScreen(ContractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
    }

    @Override
    protected void init() {
        super.init();
        requirementWidget = new TaskRequirementWidget(61, 6, menu.task().requirements(), false);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var task = menu.task();

        graphics.pose().pushMatrix();
        graphics.pose().scale(0.7f, 0.7f);

        graphics.text(font, Component.literal(task.contractor()), Math.round(8 / 0.7f), Math.round(84 / 0.7f), 0xFF000000, false);
        graphics.text(font, Component.literal(task.profession()), Math.round(8 / 0.7f), Math.round(90 / 0.7f), 0xFF404040, false);

        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5f, 0.5f);

        graphics.text(font, Component.literal("Min. level: " + task.minLevel()),
                8 * 2, 212, 0xFF404040, false);
        graphics.text(font, Component.literal("XP: +" + task.experienceReward()),
                8 * 2, 223, 0xFF404040, false);
        graphics.text(font, Component.literal("Money: +" + task.moneyReward()),
                8 * 2, 234, 0xFF404040, false);

        graphics.pose().popMatrix();

        drawCentered(graphics, Component.literal(task.name()), 125, 0xFF000000);
        List<FormattedCharSequence> description = font.split(Component.literal(task.description()), (imageWidth - 16) * 2);

        int y = 135;
        float scale = 0.5f;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        for (FormattedCharSequence line : description) {
            int x = (int) (imageWidth - font.width(line) * scale) / 2;
            graphics.text(font, line, Math.round(x / scale), Math.round(y / scale), 0xFF404040, false);
            y += 6;
        }
        graphics.pose().popMatrix();
        requirementWidget.extract(graphics, mouseX - leftPos, mouseY - topPos);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, (imageWidth - font.width(text)) / 2, y, color, false);
    }
}