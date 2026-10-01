package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.ContractMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ContractScreen extends AbstractContainerScreen<ContractMenu> {

    public ContractScreen(ContractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 280, 210);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFE5DDC8);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF554D40);
        graphics.outline(leftPos + 8, topPos + 8, imageWidth - 16, imageHeight - 16, 0xFF8A7E69);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var task = menu.task();

        graphics.centeredText(font, Component.literal(task.name()), imageWidth / 2, 14, 0xFF2E2923);
        graphics.text(font, Component.literal("From: " + task.contractor()), 14, 33, 0xFF51493D, false);
        graphics.text(font, Component.literal("Minimum Level: " + task.minLevel()), 14, 46, 0xFF51493D, false);

        int y = 65;

        for (String line : wrapText(task.description(), 55)) {
            if (y > 105) {
                break;
            }

            graphics.text(font, Component.literal(line), 14, y, 0xFF39332B, false);

            y += 11;
        }

        y = 118;

        graphics.text(font, Component.literal("Requirements:"), 14, y, 0xFF2E2923, false);

        y += 13;

        int shown = 0;

        for (var requirement : task.requirements()) {
            if (shown >= 3) {
                break;
            }

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()), 22, y, 0xFF39332B, false);

            y += 11;
            shown++;
        }

        graphics.text(font, Component.literal("Rewards: +" + task.experienceReward() + " XP, +" + task.moneyReward() + " money"), 14, 168, 0xFF51493D, false);
        graphics.centeredText(font, Component.literal("Use this Contract on a Bulletin Board to accept it."), imageWidth / 2, 190, 0xFF746957);
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
}