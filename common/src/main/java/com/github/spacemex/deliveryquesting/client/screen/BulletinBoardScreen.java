package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
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

public final class BulletinBoardScreen extends AbstractContainerScreen<BulletinBoardMenu> {
    private static final int ROWS_PER_PAGE = 4;
    private final List<Button> acceptButtons = new ArrayList<>();
    private Button previousButton;
    private Button nextButton;
    private int page;

    public BulletinBoardScreen(BulletinBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 320, 220);
    }

    @Override
    protected void init() {
        super.init();

        acceptButtons.clear();

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            final int rowIndex = row;

            Button button = Button.builder(Component.literal("Accept"), pressed -> acceptRow(rowIndex))
                    .bounds(leftPos + 235, topPos + 28 + row * 40, 70, 20).build();

            acceptButtons.add(this.addRenderableWidget(button));
        }

        previousButton = this.addRenderableWidget(Button.builder(Component.literal("<"),
                        button -> {
                            if (page > 0) {
                                page--;
                                refreshButtons();
                            }
                        })
                .bounds(leftPos + 110, topPos + 188, 35, 20).build());

        nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"),
                        button -> {
                            if (page < getPageCount() - 1) {
                                page++;
                                refreshButtons();
                            }
                        })
                .bounds(leftPos + 175, topPos + 188, 35, 20).build());

        refreshButtons();
    }

    private void acceptRow(int row) {
        int index = page * ROWS_PER_PAGE + row;

        if (index < 0 || index >= menu.tasks().size()) {

            return;
        }

        BulletinBoardTaskEntry entry = menu.tasks().get(index);

        for (Button button : acceptButtons) {

            button.active = false;
        }

        NetworkManager.sendToServer(new AcceptTaskPayload(entry.id()));
    }

    private void refreshButtons() {
        int start = page * ROWS_PER_PAGE;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int index = start + row;
            Button button = acceptButtons.get(row);

            button.active = index < menu.tasks().size();
        }

        previousButton.active = page > 0;
        nextButton.active = page < getPageCount() - 1;
    }

    private int getPageCount() {
        return Math.max(1, (menu.tasks().size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFE2C99A);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF5C3D23);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 12, 10, 0xFF3B2818, false);

        if (menu.tasks().isEmpty()) {
            graphics.text(font, Component.literal("There are no contracts currently available."), 12, 45, 0xFF5C3D23, false);
            return;
        }

        int start = page * ROWS_PER_PAGE;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int index = start + row;

            if (index >= menu.tasks().size()) {
                break;
            }

            BulletinBoardTaskEntry task = menu.tasks().get(index);
            int y = 30 + row * 40;

            graphics.text(font, Component.literal(task.name()), 12, y, 0xFF332214, false);
            graphics.text(font, Component.literal(task.contractor() + " | Level " + task.minLevel()), 12, y + 11, 0xFF674A32, false);
            graphics.text(font, Component.literal(task.experienceReward() + " XP | " + task.moneyReward() + " money"), 12, y + 22, 0xFF674A32, false);
        }

        graphics.centeredText(font, Component.literal("Page " + (page + 1) + " / " + getPageCount()), imageWidth / 2, 194, 0xFF3B2818);
    }
}