package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.widget.TaskRequirementWidget;
import com.github.spacemex.deliveryquesting.menu.ContractMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
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
    private static final int DESCRIPTION_TOP = 135;
    private static final int DESCRIPTION_BOTTOM = 211;
    private static final int DESCRIPTION_LEFT = 8;
    private static final float DESCRIPTION_SCALE = 0.5F;
    private static final int DESCRIPTION_LINE_HEIGHT = 6;
    private List<FormattedCharSequence> descriptionLines = List.of();
    private int descriptionScroll;

    public ContractScreen(ContractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
    }

    @Override
    protected void init() {
        super.init();
        requirementWidget = new TaskRequirementWidget(61, 6, menu.task().requirements(), false);
        descriptionLines = font.split(Component.literal(menu.task().description()),
                Math.round((imageWidth - DESCRIPTION_LEFT * 2) / DESCRIPTION_SCALE));
        descriptionScroll = 0;
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
        drawDescription(graphics);
        requirementWidget.extract(graphics, mouseX - leftPos, mouseY - topPos);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, (imageWidth - font.width(text)) / 2, y, color, false);
    }

    private void drawDescription(GuiGraphicsExtractor graphics) {
        int visibleLines = getVisibleDescriptionLines();
        int end = Math.min(descriptionScroll + visibleLines, descriptionLines.size());
        int y = DESCRIPTION_TOP;

        graphics.pose().pushMatrix();
        graphics.pose().scale(DESCRIPTION_SCALE, DESCRIPTION_SCALE);

        for (int i = descriptionScroll; i < end; i++) {
            FormattedCharSequence line = descriptionLines.get(i);

            int renderedWidth = font.width(line);
            int x = Math.round((imageWidth / DESCRIPTION_SCALE - renderedWidth) / 2F);

            graphics.text(font, line, x, Math.round(y / DESCRIPTION_SCALE), 0xFF404040, false);
            y += DESCRIPTION_LINE_HEIGHT;
        }

        graphics.pose().popMatrix();

        if (hasDescriptionScroll()) {
            drawDescriptionScrollbar(graphics);
        }
    }

    private void drawDescriptionScrollbar(GuiGraphicsExtractor graphics) {
        int trackX = imageWidth - 6;
        int trackY = DESCRIPTION_TOP;
        int trackHeight = DESCRIPTION_BOTTOM - DESCRIPTION_TOP;
        int visibleLines = getVisibleDescriptionLines();
        int totalLines = descriptionLines.size();
        int thumbHeight = Math.max(8, Math.round(trackHeight * (visibleLines / (float) totalLines)));
        int maxScroll = getMaxDescriptionScroll();
        int availableTravel = trackHeight - thumbHeight;
        int thumbY = trackY;

        if (maxScroll > 0) {
            thumbY += Math.round(availableTravel * (descriptionScroll / (float) maxScroll));
        }

        graphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0x44333333);
        graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFF777777);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && requirementWidget != null && requirementWidget.mouseClicked(event.x() - leftPos, event.y() - topPos)) {
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double relativeX = mouseX - leftPos;
        double relativeY = mouseY - topPos;

        if (hasDescriptionScroll() && relativeX >= DESCRIPTION_LEFT && relativeX < imageWidth - DESCRIPTION_LEFT && relativeY >= DESCRIPTION_TOP && relativeY < DESCRIPTION_BOTTOM) {
            if (scrollY > 0D) {
                descriptionScroll = Math.max(0, descriptionScroll - 1);
            } else if (scrollY < 0D) {
                descriptionScroll = Math.min(getMaxDescriptionScroll(), descriptionScroll + 1);
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int getVisibleDescriptionLines() {
        return Math.max(1, (DESCRIPTION_BOTTOM - DESCRIPTION_TOP) / DESCRIPTION_LINE_HEIGHT);
    }

    private int getMaxDescriptionScroll() {
        return Math.max(0, descriptionLines.size() - getVisibleDescriptionLines());
    }

    private boolean hasDescriptionScroll() {
        return getMaxDescriptionScroll() > 0;
    }
}