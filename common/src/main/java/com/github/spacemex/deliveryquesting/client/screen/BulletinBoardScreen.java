package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.widget.TaskRequirementWidget;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import com.github.spacemex.deliveryquesting.menu.entry.BulletinBoardTaskEntry;
import com.github.spacemex.deliveryquesting.networking.packets.ShowTaskPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class BulletinBoardScreen extends AbstractContainerScreen<BulletinBoardMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/bulletin_board.png");
    private Button previousButton;
    private Button nextButton;
    private int page;
    private TaskRequirementWidget requirementWidget;

    public BulletinBoardScreen(BulletinBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 167);
    }

    @Override
    protected void init() {
        super.init();

        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> previousPage())
                .bounds(leftPos + 6, topPos + 140, 26, 20).build());

        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> nextPage())
                .bounds(leftPos + 144, topPos + 140, 26, 20).build());

        updateButtons();
        updateTaskWidget();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,
                256, 256);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 7, topPos + 25,
                0, 223, 162, 5, 256, 256);

        double level = menu.groupLevel();
        double progress = level - Math.floor(level);

        int progressWidth = (int) (162D * progress);

        if (progressWidth > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 7, topPos + 25, 0, 228, progressWidth, 5, 256, 256);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        drawCentered(graphics, Component.literal("Experience"), 8, 0xFF404040);
        drawLevel(graphics);

        List<BulletinBoardTaskEntry> tasks = menu.activeTasks();

        if (tasks.isEmpty()) {
            drawCentered(graphics, Component.literal("No active contracts"), 65, 0xFF404040);
            return;
        }

        if (requirementWidget != null) {
            requirementWidget.extract(graphics, mouseX - leftPos, mouseY - topPos);
        }

        drawCentered(graphics, Component.literal("Page " + (page + 1) + " of " + tasks.size()), 145, 0xFF404040);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, (imageWidth - font.width(text)) / 2, y, color, false);
    }

    private void drawLevel(GuiGraphicsExtractor graphics) {
        Component text = Component.literal(String.valueOf((int) Math.floor(menu.groupLevel())));

        int x = (imageWidth - font.width(text)) / 2;

        graphics.text(font, text, x + 1, 20, 0xFF000000, false);
        graphics.text(font, text, x - 1, 20, 0xFF000000, false);
        graphics.text(font, text, x, 21, 0xFF000000, false);
        graphics.text(font, text, x, 19, 0xFF000000, false);
        graphics.text(font, text, x, 20, 0xFFFFFFFF, false);
    }

    private void previousPage() {
        int size = menu.activeTasks().size();

        if (size <= 0) {
            return;
        }

        page = Math.floorMod(page - 1, size);

        updateButtons();
        updateTaskWidget();
    }

    private void nextPage() {
        int size = menu.activeTasks().size();

        if (size <= 0) {
            return;
        }

        page = Math.floorMod(page + 1, size);

        updateButtons();
        updateTaskWidget();
    }

    private void updateButtons() {
        int size = menu.activeTasks().size();
        if (size <= 0) {
            page = 0;
        } else if (page >= size) {
            page = size - 1;
        }
        boolean visible = size > 1;
        previousButton.visible = visible;
        nextButton.visible = visible;
    }

    private void updateTaskWidget() {
        List<BulletinBoardTaskEntry> tasks = menu.activeTasks();

        if (tasks.isEmpty()) {
            requirementWidget = null;
            return;
        }

        BulletinBoardTaskEntry task = tasks.get(page);

        requirementWidget = new TaskRequirementWidget(35, 35, task.requirements(), true,
                () -> NetworkManager.sendToServer(new ShowTaskPayload(task.id())));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && requirementWidget != null && requirementWidget.mouseClicked(event.x() - leftPos, event.y() - topPos)) {
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }
}