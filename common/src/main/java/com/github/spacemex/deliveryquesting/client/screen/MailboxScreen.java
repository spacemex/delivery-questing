package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.MailboxMenu;
import com.github.spacemex.deliveryquesting.menu.MailboxParcelEntry;
import com.github.spacemex.deliveryquesting.networking.packets.CollectParcelPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class MailboxScreen extends AbstractContainerScreen<MailboxMenu> {
    private static final int MAX_ROWS = 4;
    private final List<Button> collectButtons = new ArrayList<>();

    public MailboxScreen(MailboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 280, 180);
    }

    @Override
    protected void init() {
        super.init();

        collectButtons.clear();

        for (int row = 0; row < MAX_ROWS; row++) {
            final int rowIndex = row;

            Button button = Button.builder(Component.literal("Collect"), pressed -> collect(rowIndex))
                    .bounds(leftPos + 195, topPos + 38 + row * 29, 70, 20).build();

            collectButtons.add(this.addRenderableWidget(button));
        }

        refreshButtons();
    }

    private void collect(int row) {
        if (row < 0 || row >= menu.parcels().size()) {
            return;
        }

        MailboxParcelEntry entry = menu.parcels().get(row);

        for (Button button : collectButtons) {
            button.active = false;
        }

        NetworkManager.sendToServer(new CollectParcelPayload(entry.id()));
    }

    private void refreshButtons() {
        for (int row = 0; row < MAX_ROWS; row++) {
            collectButtons.get(row).active = row < menu.parcels().size();
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFD7D7D7);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF454545);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 12, 10, 0xFF222222, false);
        graphics.text(font, Component.literal("Inbox"), 12, 25, 0xFF555555, false);

        if (menu.parcels().isEmpty()) {
            graphics.text(font, Component.literal("No mail."), 12, 48, 0xFF777777, false);
            return;
        }

        for (int row = 0; row < menu.parcels().size(); row++) {
            MailboxParcelEntry parcel = menu.parcels().get(row);
            int y = 42 + row * 29;

            graphics.text(font, Component.literal("From: " + parcel.sender()), 12, y, 0xFF222222, false);
            graphics.text(font, Component.literal(parcel.itemCount() + " item(s)"), 12, y + 11, 0xFF666666, false);
        }
    }
}