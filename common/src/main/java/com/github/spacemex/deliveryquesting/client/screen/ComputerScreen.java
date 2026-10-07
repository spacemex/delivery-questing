package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerInboxEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerJobMailEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerMailEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerOfferEntry;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailContractPayload;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailJobPayload;
import com.github.spacemex.deliveryquesting.networking.packets.BuyOfferPayload;
import com.github.spacemex.deliveryquesting.networking.packets.MarkEmailReadPayload;
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

import java.util.*;

public final class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final int MAILS_PER_PAGE = 4;
    private View view = View.DESKTOP;
    private int mailPage;
    private final Set<UUID> readThisSession = new HashSet<>();
    private static final int OFFERS_PER_PAGE = 4;
    private int minazonPage;
    private UUID selectedMailId;
    private static final Identifier COMPUTER_TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID,
            "textures/gui/container/computer.png");
    private static final Identifier DESKTOP_TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID,
            "textures/gui/computer/desktop.png");
    private static final Identifier DESKTOP_ICONS = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID,
            "textures/gui/computer/icons.png");
    private static final int ICON_SIZE = 32;
    private static final int MINTERNET_X = 16;
    private static final int MINTERNET_Y = 16;
    private static final int MAIL_X = 64;
    private static final int MAIL_Y = 16;
    private int desktopMouseX;
    private int desktopMouseY;

    private enum View {
        DESKTOP,
        MAIL_LIST,
        MAIL_DETAIL,
        JOB_DETAIL,
        MINAZON
    }

    public ComputerScreen(ComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 194);
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
            case JOB_DETAIL -> buildJobDetail();
            case MINAZON -> buildMinazon();
        }
    }

    private void buildDesktop() {

    }

    private void buildMailList() {
        List<ComputerInboxEntry> inbox = menu.inbox();

        int start = mailPage * MAILS_PER_PAGE;

        for (int row = 0; row < MAILS_PER_PAGE; row++) {
            int index = start + row;

            if (index >= inbox.size()) {
                break;
            }

            ComputerInboxEntry entry = inbox.get(index);

            String prefix = isRead(entry) ? "" : "* ";

            this.addRenderableWidget(Button.builder(Component.literal(prefix + entry.title() + " - " + entry.sender()),
                    button -> openMail(entry)
            ).bounds(leftPos + 15, topPos + 35 + row * 27, 226, 22).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Desktop"), button -> {
                    view = View.DESKTOP;
                    selectedMailId = null;
                    rebuildView();
                }).bounds(leftPos + 15, topPos + 153, 70, 22).build()
        );

        Button previous = this.addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            mailPage--;
            rebuildView();
        }).bounds(leftPos + 93, topPos + 153, 70, 22).build());

        previous.active = mailPage > 0;

        Button next = this.addRenderableWidget(Button.builder(Component.literal("Next"
        ), button -> {
            mailPage++;
            rebuildView();
        }).bounds(leftPos + 171, topPos + 153, 70, 22).build());

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
        }).bounds(
                leftPos + 15,
                topPos + 153,
                90,
                22
        ).build());

        Button accept = this.addRenderableWidget(Button.builder(Component.literal(entry.canAccept() ? "Accept Contract" : "Already Handled"), button -> {
            button.active = false;
            NetworkManager.sendToServer(new AcceptEmailContractPayload(entry.emailId()));
        }).bounds(
                leftPos + 131,
                topPos + 153,
                110,
                22
        ).build());

        accept.active = entry.canAccept();
    }

    private void buildMinazon() {
        List<ComputerOfferEntry> offers = menu.offers();

        int start = minazonPage * OFFERS_PER_PAGE;

        for (int row = 0; row < OFFERS_PER_PAGE; row++) {
            int index = start + row;

            if (index >= offers.size()) {
                break;
            }

            ComputerOfferEntry offer = offers.get(index);

            String label = offer.item() + " x" + offer.count() + " - " + offer.price();

            if (offer.forEveryMember()) {
                label += " [each member]";
            }

            if (!offer.unlocked()) {
                label += " [Lv " + offer.minLevel() + "]";
            }

            Button button = this.addRenderableWidget(Button.builder(
                    Component.literal(label), pressed -> {
                        pressed.active = false;
                        NetworkManager.sendToServer(new BuyOfferPayload(offer.id()));
                    }
            ).bounds(leftPos + 15, topPos + 38 + row * 27, 226, 22).build());

            button.active = offer.canBuy();
        }

        this.addRenderableWidget(Button.builder(Component.literal("Desktop"), button -> {
            view = View.DESKTOP;
            rebuildView();
        }).bounds(leftPos + 15, topPos + 153, 80, 22).build());

        Button previous = this.addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            minazonPage--;
            rebuildView();
        }).bounds(leftPos + 110, topPos + 153, 80, 22).build());

        previous.active = minazonPage > 0;

        Button next = this.addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            minazonPage++;
            rebuildView();
        }).bounds(leftPos + 205, topPos + 153, 80, 22).build());

        next.active = minazonPage < getOfferPageCount() - 1;
    }

    private void openMail(ComputerInboxEntry entry) {
        if (!isRead(entry)) {
            readThisSession.add(entry.emailId());

            NetworkManager.sendToServer(new MarkEmailReadPayload(entry.emailId()));
        }

        switch (entry.type()) {
            case CONTRACT -> {
                selectedMailId = entry.emailId();
                view = View.MAIL_DETAIL;
            }
            case OFFER -> {
                selectedMailId = null;
                view = View.MINAZON;
                minazonPage = findOfferPage(entry.referenceId());
            }
            case JOB -> {
                selectedMailId = entry.emailId();
                view = View.JOB_DETAIL;
            }
        }

        rebuildView();
    }

    private ComputerMailEntry getSelectedMail() {
        if (selectedMailId == null) {
            return null;
        }
        return menu.mail().stream().filter(entry -> entry.emailId().equals(selectedMailId)).findFirst().orElse(null);
    }

    private boolean isRead(ComputerInboxEntry entry) {
        return entry.read() || readThisSession.contains(entry.emailId());
    }

    private long getUnreadMailCount() {
        return menu.inbox().stream().filter(entry -> !isRead(entry)).count();
    }

    private int getMailPageCount() {
        return Math.max(1, (menu.inbox().size() + MAILS_PER_PAGE - 1) / MAILS_PER_PAGE);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, COMPUTER_TEXTURE,
                leftPos, topPos, 0, 0, 256, 194, 256, 256);

        if (view == View.DESKTOP) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, DESKTOP_TEXTURE,
                    leftPos + 3, topPos + 3, 0, 0, 250, 188, 256, 256);
            return;
        }

        graphics.fill(leftPos + 3, topPos + 3, leftPos + 253, topPos + 191, 0xFF263238);
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        desktopMouseX = mouseX - leftPos;
        desktopMouseY = mouseY - topPos;

        switch (view) {
            case DESKTOP -> extractDesktopLabels(graphics);
            case MAIL_LIST -> extractMailListLabels(graphics);
            case MAIL_DETAIL -> extractMailDetailLabels(graphics);
            case JOB_DETAIL -> extractJobDetailLabels(graphics);
            case MINAZON -> extractMinazonLabels(graphics);
        }
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event, boolean doubleClick) {
        if (view == View.DESKTOP && event.button() == 0) {
            double mouseX = event.x() - leftPos;
            double mouseY = event.y() - topPos;

            if (isInside(mouseX, mouseY, MINTERNET_X, MINTERNET_Y, ICON_SIZE, ICON_SIZE)) {
                view = View.MINAZON;
                minazonPage = 0;
                rebuildView();
                return true;
            }

            if (isInside(mouseX, mouseY, MAIL_X, MAIL_Y, ICON_SIZE, ICON_SIZE)) {
                view = View.MAIL_LIST;
                mailPage = 0;
                selectedMailId = null;
                rebuildView();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isDesktopIconHovered(int x, int y) {
        return desktopMouseX >= x && desktopMouseX < x + ICON_SIZE && desktopMouseY >= y && desktopMouseY < y + ICON_SIZE;
    }

    private void extractJobDetailLabels(GuiGraphicsExtractor graphics) {
        ComputerJobMailEntry entry = getSelectedJobMail();

        if (entry == null) {
            return;
        }

        var job = entry.job();

        graphics.text(font, Component.literal(job.name()), 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal("Repeatable Job"), 200, 12, 0xFFFFC107, false);
        graphics.text(font, Component.literal("From: " + job.contractor()), 14, 27, 0xFF80CBC4, false);

        int y = 45;

        for (String line : wrapText(job.description(), 48)) {
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

        for (var requirement : job.requirements()) {
            if (shown >= 3) {
                break;
            }

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()), 20, y, 0xFFD7E1E5, false);

            y += 11;
            shown++;
        }

        graphics.text(font, Component.literal("Rewards: +" + job.experienceReward() + " XP, +" + job.moneyReward() + " money"), 14, 139, 0xFF80CBC4, false);
    }

    private void extractDesktopLabels(GuiGraphicsExtractor graphics) {
        drawDesktopIcon(graphics, MINTERNET_X, MINTERNET_Y, 0, Component.literal("Minternet"));
        drawDesktopIcon(graphics, MAIL_X, MAIL_Y, 32, Component.literal("Mail"));
        drawDesktopCount(graphics, MAIL_X, MAIL_Y, getUnreadMailCount());
    }

    private void extractMailListLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.literal("Mail"), 14, 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal("Inbox - " + getUnreadMailCount() + " unread"), 14, 23, 0xFF80CBC4, false);

        if (menu.inbox().isEmpty()) {
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

    private void extractMinazonLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.literal("Minazon"), 14, 12, 0xFFFFFFFF, false);

        graphics.text(font, Component.literal("Balance: " + menu.balance()), 14, 24, 0xFF80CBC4, false);

        if (menu.offers().isEmpty()) {
            graphics.centeredText(font, Component.literal("No offers available."), imageWidth / 2, 85, 0xFF9EA7AA);
        }

        graphics.centeredText(font, Component.literal("Orders arrive the next Minecraft morning"), imageWidth / 2, 142, 0xFF9EA7AA);
    }

    private void drawDesktopIcon(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int textureY,
            Component name
    ) {
        boolean hovered =
                isDesktopIconHovered(
                        x,
                        y
                );

        /*
         * The original 1.18 GUI blit overload implicitly
         * used a 256x256 texture UV space.
         */
        if (hovered) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    DESKTOP_ICONS,
                    x,
                    y,
                    32,
                    textureY,
                    ICON_SIZE,
                    ICON_SIZE,
                    256,
                    256
            );
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESKTOP_ICONS,
                x,
                y,
                0,
                textureY,
                ICON_SIZE,
                ICON_SIZE,
                256,
                256
        );

        int center =
                x
                        + ICON_SIZE / 2;

        graphics.text(
                font,
                name,
                center
                        - font.width(name) / 2
                        + 1,
                y
                        + ICON_SIZE
                        + 2,
                0xFF404040,
                false
        );

        graphics.text(
                font,
                name,
                center
                        - font.width(name) / 2,
                y
                        + ICON_SIZE
                        + 1,
                0xFFFFFFFF,
                false
        );
    }

    private void drawDesktopCount(GuiGraphicsExtractor graphics, int x, int y, long count) {
        if (count <= 0L) {
            return;
        }

        Component text = Component.literal(Long.toString(count));

        graphics.text(font, text, x + ICON_SIZE - 1 - font.width(text), y + 4, 0xFFAA0000, false);
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

    private int getOfferPageCount() {
        return Math.max(1, (menu.offers().size() + OFFERS_PER_PAGE - 1) / OFFERS_PER_PAGE);
    }

    private int findOfferPage(Identifier offerId) {
        for (int i = 0; i < menu.offers().size(); i++) {
            if (menu.offers().get(i).id().equals(offerId)) {
                return i / OFFERS_PER_PAGE;
            }
        }

        return 0;
    }

    private ComputerJobMailEntry getSelectedJobMail() {
        if (selectedMailId == null) {
            return null;
        }
        return menu.jobMail().stream().filter(entry -> entry.emailId().equals(selectedMailId)).findFirst().orElse(null);
    }

    private void buildJobDetail() {
        ComputerJobMailEntry entry = getSelectedJobMail();

        if (entry == null) {
            view = View.MAIL_LIST;
            rebuildView();
            return;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
                    view = View.MAIL_LIST;
                    rebuildView();
                }
        ).bounds(
                leftPos + 15,
                topPos + 153,
                90,
                22
        ).build());

        Button accept = this.addRenderableWidget(Button.builder(Component.literal(entry.canAccept() ? "Accept Job" : "Unavailable"), button -> {
            button.active = false;
            NetworkManager.sendToServer(new AcceptEmailJobPayload(entry.emailId()));
        }).bounds(
                leftPos + 131,
                topPos + 153,
                110,
                22
        ).build());

        accept.active = entry.canAccept();
    }
}