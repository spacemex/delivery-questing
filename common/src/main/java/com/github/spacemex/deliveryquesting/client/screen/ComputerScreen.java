package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.menu.ComputerMailEntry;
import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailContractPayload;
import com.github.spacemex.deliveryquesting.networking.packets.MarkEmailReadPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final int MAILS_PER_PAGE = 4;
    private View view = View.DESKTOP;
    private int mailPage;
    private int selectedMail = -1;
    private final Set<UUID> readThisSession = new HashSet<>();

    private enum View {
        DESKTOP,
        MAIL_LIST,
        MAIL_DETAIL
    }

    public ComputerScreen(ComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 300, 190);
    }

    @Override
    protected void init() {
        super.init();

        rebuildView();
    }

    private void rebuildView() {
        clearWidgets();

        switch (view) {
            case DESKTOP -> buildDesktop();
            case MAIL_LIST -> buildMailList();
            case MAIL_DETAIL -> buildMailDetail();
        }
    }

    private void buildDesktop() {
        this.addRenderableWidget(Button.builder(Component.literal("Mail (" + getUnreadMailCount() + ")"), button -> {
            view = View.MAIL_LIST;
            mailPage = 0;
            selectedMail = -1;
            rebuildView();
        }).bounds(leftPos + 25, topPos + 118, 110, 24).build());

        Button minazon = this.addRenderableWidget(Button.builder(Component.literal("Minazon"),
                button -> {
                }).bounds(leftPos + 165, topPos + 118, 110, 24).build());

        minazon.active = false;
    }

    private void buildMailList() {
        List<ComputerMailEntry> mail = menu.mail();
        int start = mailPage * MAILS_PER_PAGE;

        for (int row = 0; row < MAILS_PER_PAGE; row++) {
            int index = start + row;

            if (index >= mail.size()) {
                break;
            }

            ComputerMailEntry entry = mail.get(index);

            String prefix = isRead(entry) ? "" : "* ";

            this.addRenderableWidget(Button.builder(Component.literal(prefix + entry.task().name() + " - " + entry.task().contractor()),
                    button -> openMail(index)).bounds(leftPos + 15, topPos + 35 + row * 27, 270, 22).build());
        }

        this.addRenderableWidget(
                Button.builder(Component.literal("Desktop"), button -> {
                    view = View.DESKTOP;
                    selectedMail = -1;
                    rebuildView();
                }).bounds(leftPos + 15, topPos + 153, 80, 22).build());

        Button previous = this.addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            mailPage--;
            rebuildView();
        }).bounds(leftPos + 110, topPos + 153, 80, 22).build());

        previous.active = mailPage > 0;

        Button next = this.addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            mailPage++;
            rebuildView();
        }).bounds(leftPos + 205, topPos + 153, 80, 22).build());

        next.active = mailPage < getMailPageCount() - 1;
    }

    private void buildMailDetail() {
        ComputerMailEntry entry = getSelectedMail();

        if (entry == null) {
            view = View.MAIL_LIST;
            rebuildView();
            return;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            view = View.MAIL_LIST;
            rebuildView();
        }).bounds(leftPos + 15, topPos + 153, 90, 22).build());

        Button accept = this.addRenderableWidget(Button.builder(Component.literal(entry.canAccept() ? "Accept Contract" : "Already Handled"), button -> {
            button.active = false;
            NetworkManager.sendToServer(new AcceptEmailContractPayload(entry.emailId()));
        }).bounds(leftPos + 165, topPos + 153, 120, 22).build());

        accept.active = entry.canAccept();
    }

    private void openMail(int index) {
        if (index < 0 || index >= menu.mail().size()) {
            return;
        }

        ComputerMailEntry entry = menu.mail().get(index);
        selectedMail = index;

        if (!isRead(entry)) {
            readThisSession.add(entry.emailId());
            NetworkManager.sendToServer(new MarkEmailReadPayload(entry.emailId()));
        }

        view = View.MAIL_DETAIL;
        rebuildView();
    }

    private ComputerMailEntry getSelectedMail() {
        if (selectedMail < 0 || selectedMail >= menu.mail().size()) {
            return null;
        }

        return menu.mail().get(selectedMail);
    }

    private boolean isRead(ComputerMailEntry entry) {
        return entry.read() || readThisSession.contains(entry.emailId());
    }

    private long getUnreadMailCount() {
        return menu.mail().stream().filter(entry -> !isRead(entry)).count();
    }

    private int getMailPageCount() {
        return Math.max(1, (menu.mail().size() + MAILS_PER_PAGE - 1) / MAILS_PER_PAGE);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF1B1E23);
        graphics.fill(leftPos + 8, topPos + 8, leftPos + imageWidth - 8, topPos + imageHeight - 8, 0xFF263238);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF707A80);
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        switch (view) {
            case DESKTOP -> extractDesktopLabels(graphics);
            case MAIL_LIST -> extractMailListLabels(graphics);
            case MAIL_DETAIL -> extractMailDetailLabels(graphics);
        }
    }

    private void extractDesktopLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, title, 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal(menu.groupName()), 14, 33, 0xFF80CBC4, false);
        graphics.text(font, Component.literal("Delivery Level: " + menu.level()), 14, 54, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Balance: " + menu.balance()), 14, 68, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Active Contracts: " + menu.activeTasks()), 14, 82, 0xFFD7E1E5, false);
        graphics.text(font, Component.literal("Completed Contracts: " + menu.completedTasks()), 14, 96, 0xFFD7E1E5, false);
        graphics.centeredText(font, Component.literal("Delivery Computer"), imageWidth / 2, 156, 0xFF9EA7AA);
    }

    private void extractMailListLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.literal("Mail"), 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal("Inbox - " + getUnreadMailCount() + " unread"), 14, 23, 0xFF80CBC4, false);

        if (menu.mail().isEmpty()) {
            graphics.centeredText(font, Component.literal("No mail."), imageWidth / 2, 85, 0xFF9EA7AA);
        }
    }

    private void extractMailDetailLabels(GuiGraphicsExtractor graphics) {
        ComputerMailEntry entry = getSelectedMail();

        if (entry == null) {
            return;
        }

        var task = entry.task();

        graphics.text(font, Component.literal(task.name()), 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal("From: " + task.contractor()), 14, 27, 0xFF80CBC4, false);

        int y = 45;

        for (String line : wrapText(task.description(), 48)) {

            if (y > 82) {
                break;
            }

            graphics.text(font, Component.literal(line), 14, y, 0xFFD7E1E5, false);

            y += 11;
        }

        y = 91;

        graphics.text(font, Component.literal("Requirements:"), 14, y, 0xFFFFFFFF, false);

        y += 12;

        int shown = 0;

        for (var requirement : task.requirements()) {

            if (shown >= 3) {
                break;
            }

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()), 20, y, 0xFFD7E1E5, false);

            y += 11;
            shown++;
        }
        graphics.text(font, Component.literal("Rewards: +" + task.experienceReward() + " XP, +" + task.moneyReward() + " money"), 14, 139, 0xFF80CBC4, false);
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