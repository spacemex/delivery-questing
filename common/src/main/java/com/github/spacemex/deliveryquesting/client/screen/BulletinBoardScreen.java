package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardRequirementEntry;
import com.github.spacemex.deliveryquesting.menu.BulletinBoardTaskEntry;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptTaskPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class BulletinBoardScreen extends AbstractContainerScreen<BulletinBoardMenu> {
    private static final int ROWS_PER_PAGE = 4;
    private final List<Button> actionButtons = new ArrayList<>();
    private Button availableTab;
    private Button activeTab;
    private Button previousButton;
    private Button nextButton;
    private View view = View.AVAILABLE;
    private int page;

    public BulletinBoardScreen(BulletinBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 320, 220);
    }

    @Override
    protected void init() {
        super.init();

        actionButtons.clear();

        availableTab = this.addRenderableWidget(Button.builder(Component.literal("Available"), button -> setView(View.AVAILABLE))
                .bounds(leftPos + 12, topPos + 24, 85, 20).build());

        activeTab = this.addRenderableWidget(Button.builder(Component.literal("Active"), button -> setView(View.ACTIVE))
                .bounds(leftPos + 102, topPos + 24, 85, 20).build());

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            final int rowIndex = row;
            Button button = Button.builder(Component.literal("Accept"), pressed -> handleRow(rowIndex))
                    .bounds(leftPos + 238, topPos + 53 + row * 34, 68, 20).build();

            actionButtons.add(this.addRenderableWidget(button));
        }

        previousButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                    if (page > 0) {
                        page--;
                        refreshButtons();
                    }
                }
        ).bounds(leftPos + 110, topPos + 188, 35, 20).build());

        nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            if (page < getPageCount() - 1) {
                page++;
                refreshButtons();
            }
        }).bounds(leftPos + 175, topPos + 188, 35, 20).build());

        refreshButtons();
    }

    private void setView(View newView) {
        if (view == newView) {
            return;
        }

        view = newView;
        page = 0;

        refreshButtons();
    }

    private void handleRow(int row) {
        if (view != View.AVAILABLE) {
            return;
        }

        int index = page * ROWS_PER_PAGE + row;

        List<BulletinBoardTaskEntry> tasks = currentTasks();

        if (index < 0 || index >= tasks.size()) {
            return;
        }

        BulletinBoardTaskEntry entry = tasks.get(index);
        disableActionButtons();
        NetworkManager.sendToServer(new AcceptTaskPayload(entry.id()));
    }

    private void disableActionButtons() {
        for (Button button : actionButtons) {
            button.active = false;
        }
    }

    private void refreshButtons() {
        List<BulletinBoardTaskEntry> tasks = currentTasks();
        int start = page * ROWS_PER_PAGE;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int index = start + row;
            Button button = actionButtons.get(row);
            boolean visible = view == View.AVAILABLE && index < tasks.size();

            button.setMessage(Component.literal("Accept"));
            button.visible = visible;
            button.active = visible;
        }

        availableTab.active = view != View.AVAILABLE;
        activeTab.active = view != View.ACTIVE;
        previousButton.active = page > 0;
        nextButton.active = page < getPageCount() - 1;
    }

    private List<BulletinBoardTaskEntry> currentTasks() {
        return switch (view) {
            case AVAILABLE -> menu.availableTasks();

            case ACTIVE -> menu.activeTasks();
        };
    }

    private int getPageCount() {
        return Math.max(1, (currentTasks().size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFE2C99A);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF5C3D23);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 12, 9, 0xFF3B2818, false);

        List<BulletinBoardTaskEntry> tasks = currentTasks();

        if (tasks.isEmpty()) {
            String message = view == View.AVAILABLE ? "There are no contracts currently available." : "Your group has no active contracts.";

            graphics.text(font, Component.literal(message), 12, 63, 0xFF5C3D23, false);

            drawPageNumber(graphics);
            return;
        }

        int start = page * ROWS_PER_PAGE;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {

            int index = start + row;

            if (index >= tasks.size()) {
                break;
            }

            BulletinBoardTaskEntry task = tasks.get(index);

            int y = 55 + row * 34;

            graphics.text(font, Component.literal(task.name()), 12, y, 0xFF332214, false);

            if (view == View.AVAILABLE) {
                drawAvailableTask(graphics, task, y);
            } else {
                drawActiveTask(graphics, task, y);
            }
        }

        drawPageNumber(graphics);
    }

    private void drawAvailableTask(GuiGraphicsExtractor graphics, BulletinBoardTaskEntry task, int y) {
        graphics.text(font, Component.literal(task.contractor() + " | Level " + task.minLevel()),
                12, y + 10, 0xFF674A32, false);
        graphics.text(font, Component.literal(task.experienceReward() + " XP | " + task.moneyReward() + " money"),
                12, y + 20, 0xFF674A32, false);
    }

    private void drawActiveTask(GuiGraphicsExtractor graphics, BulletinBoardTaskEntry task, int y) {
        graphics.text(font, Component.literal(progressText(task)), 12, y + 10, 0xFF674A32, false);
        graphics.text(font, Component.literal(task.experienceReward() + " XP | " + task.moneyReward() + " money"),
                12, y + 20, 0xFF674A32, false);
    }

    private String progressText(BulletinBoardTaskEntry task) {
        if (task.requirements().isEmpty()) {
            return "No requirements";
        }

        StringJoiner joiner = new StringJoiner(" | ");
        int shown = Math.min(2, task.requirements().size());

        for (int i = 0; i < shown; i++) {
            BulletinBoardRequirementEntry requirement = task.requirements().get(i);

            joiner.add(requirement.label() + " " + requirement.current() + "/" + requirement.required());
        }

        int hidden = task.requirements().size() - shown;

        if (hidden > 0) {
            joiner.add("+" + hidden + " more");
        }

        return joiner.toString();
    }

    private void drawPageNumber(GuiGraphicsExtractor graphics) {
        graphics.centeredText(font, Component.literal("Page " + (page + 1) + " / " + getPageCount()), imageWidth / 2, 194, 0xFF3B2818);
    }

    private enum View {
        AVAILABLE,
        ACTIVE
    }
}