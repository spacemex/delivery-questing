package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.ContractMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ContractScreen extends AbstractContainerScreen<ContractMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/contract.png");

    public ContractScreen(ContractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var task = menu.task();

        graphics.text(font, Component.literal(task.contractor()), 8, 84, 0xFF404040, false);
        graphics.text(font, Component.literal("Min. level " + task.minLevel()), 8, 95, 0xFF404040, false);
        graphics.text(font, Component.literal("+" + task.experienceReward() + " XP"), 8, 106, 0xFF404040, false);
        graphics.text(font, Component.literal("Requirements"), 65, 10, 0xFF202020, false);

        int requirementY = 24;
        int shown = Math.min(task.requirements().size(), 6);

        for (int i = 0; i < shown; i++) {
            var requirement = task.requirements().get(i);

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()),
                    65, requirementY, 0xFF404040, false);

            requirementY += 11;
        }

        drawCentered(graphics, Component.literal(task.name()), 114, 0xFF000000);

        int y = 130;

        for (String line : wrapText(task.description(), 48)) {
            if (y > 175) {
                break;
            }

            graphics.text(font, Component.literal(line), 8, y, 0xFF404040, false);

            y += 10;
        }

        graphics.text(font, Component.literal("Rewards: +" + task.experienceReward() + " XP, +"
                + task.moneyReward() + " money"), 8, 187, 0xFF404040, false);
        Component instruction = Component.literal("Use on a Bulletin Board to accept");

        graphics.text(font, instruction, (imageWidth - font.width(instruction)) / 2, 205, 0xFF665E52, false);
    }

    private static List<String> wrapText(String text, int maxCharacters) {
        List<String> lines = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        for (String word : text.split("\\s+")) {
            if (!current.isEmpty() && current.length() + word.length() + 1 > maxCharacters) {

                lines.add(current.toString());

                current.setLength(0);
            }

            if (!current.isEmpty()) {
                current.append(' ');
            }

            current.append(word);
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return lines;
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, (imageWidth - font.width(text)) / 2, y, color, false);
    }
}