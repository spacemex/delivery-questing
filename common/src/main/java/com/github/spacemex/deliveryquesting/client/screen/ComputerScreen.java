package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.ContractorSkinManager;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {

    private static final int MAILS_PER_PAGE = 5;
    private int mailOffset;

    private View view = View.DESKTOP;

    private int mailPage;

    private final Set<UUID> readThisSession = new HashSet<>();

    private static final int OFFERS_PER_PAGE = 4;
    private int minazonPage;

    private UUID selectedMailId;

    private static final Identifier COMPUTER_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/computer.png");
    private static final Identifier DESKTOP_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/desktop.png");
    private static final Identifier DESKTOP_ICONS =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/icons.png");

    private static final int ICON_SIZE = 32;
    private static final int MINTERNET_X = 16;
    private static final int MINTERNET_Y = 16;
    private static final int MAIL_X = 64;
    private static final int MAIL_Y = 16;
    private int desktopMouseX;
    private int desktopMouseY;

    private static final Identifier MAIL_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/mail.png");
    private static final Identifier MINAZON_ICON =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/minazon_icon.png");
    private static final Identifier ACCEPTED_TASK =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/accepted_task.png");

    private static final int MAIL_ROW_X = 3;
    private static final int MAIL_ROW_Y = 12;
    private static final int MAIL_ROW_WIDTH = 239;
    private static final int MAIL_ROW_HEIGHT = 33;
    private static final int MAIL_CLOSE_X = 244;
    private static final int MAIL_CLOSE_Y = 3;

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

        Button accept = this.addRenderableWidget(Button.builder(Component.literal(entry.canAccept()
                ? "Accept Contract" : "Already Handled"), button -> {
            button.active = false;

            NetworkManager.sendToServer(new AcceptEmailContractPayload(entry.emailId()));
        }).bounds(leftPos + 131, topPos + 153, 110, 22).build());

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

        return menu.mail().stream().filter(entry -> entry.emailId().equals(selectedMailId))
                .findFirst().orElse(null);
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

        if (view == View.MAIL_LIST) {
            drawMailBackground(graphics, mouseX - leftPos, mouseY - topPos);

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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x() - leftPos;
            double mouseY = event.y() - topPos;

            if (view == View.DESKTOP) {
                if (isInside(mouseX, mouseY, MINTERNET_X, MINTERNET_Y, ICON_SIZE, ICON_SIZE)) {
                    view = View.MINAZON;

                    minazonPage = 0;

                    rebuildView();

                    return true;
                }

                if (isInside(mouseX, mouseY, MAIL_X, MAIL_Y, ICON_SIZE, ICON_SIZE)) {
                    view = View.MAIL_LIST;

                    mailOffset = 0;

                    selectedMailId = null;

                    rebuildView();

                    return true;
                }
            }

            if (view == View.MAIL_LIST) {
                if (isInside(mouseX, mouseY, MAIL_CLOSE_X, MAIL_CLOSE_Y, 9, 9)) {
                    view = View.DESKTOP;

                    selectedMailId = null;

                    rebuildView();

                    return true;
                }

                List<ComputerInboxEntry> inbox = menu.inbox();

                for (int row = 0; row < MAILS_PER_PAGE; row++) {
                    int index = mailOffset + row;

                    if (index >= inbox.size()) {
                        break;
                    }

                    int rowY = MAIL_ROW_Y + row * MAIL_ROW_HEIGHT;

                    if (!isInside(mouseX, mouseY, MAIL_ROW_X, rowY, MAIL_ROW_WIDTH, MAIL_ROW_HEIGHT)) {
                        continue;
                    }

                    openMail(inbox.get(index));

                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (view == View.MAIL_LIST) {
            int maxOffset = Math.max(0, menu.inbox().size() - MAILS_PER_PAGE);

            if (maxOffset > 0) {
                if (scrollY < 0D) {
                    mailOffset = Math.min(mailOffset + 1, maxOffset);
                } else if (scrollY > 0D) {
                    mailOffset = Math.max(mailOffset - 1, 0);
                }

                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isDesktopIconHovered(int x, int y) {
        return desktopMouseX >= x && desktopMouseX < x + ICON_SIZE && desktopMouseY >= y
                && desktopMouseY < y + ICON_SIZE;
    }

    private void extractJobDetailLabels(GuiGraphicsExtractor graphics) {
        ComputerJobMailEntry entry = getSelectedJobMail();

        if (entry == null) {
            return;
        }

        var job = entry.job();

        graphics.text(font, Component.literal(job.name()), 14, 12, 0xFFFFFFFF, false);

        graphics.text(font, Component.literal("Repeatable Job"), 200, 12, 0xFFFFC107, false);

        graphics.text(font, Component.literal("From: " + job.contractor()), 14, 27,
                0xFF80CBC4, false);

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

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()),
                    20, y, 0xFFD7E1E5, false);

            y += 11;

            shown++;
        }

        graphics.text(font, Component.literal("Rewards: +" + job.experienceReward() + " XP, +"
                + job.moneyReward() + " money"), 14, 139, 0xFF80CBC4, false);
    }

    private void extractDesktopLabels(GuiGraphicsExtractor graphics) {
        drawDesktopIcon(graphics, MINTERNET_X, MINTERNET_Y, 0, Component.literal("Minternet"));

        drawDesktopIcon(graphics, MAIL_X, MAIL_Y, 32, Component.literal("Mail"));

        drawDesktopCount(graphics, MAIL_X, MAIL_Y, getUnreadMailCount());
    }

    private void extractMailListLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.literal("E-Mail"), 5, 4, 0xFFFFFFFF, false);

        List<ComputerInboxEntry> inbox = menu.inbox();

        for (int row = 0; row < MAILS_PER_PAGE; row++) {
            int index = mailOffset + row;

            if (index >= inbox.size()) {
                break;
            }

            ComputerInboxEntry entry = inbox.get(index);

            int rowY = MAIL_ROW_Y + row * MAIL_ROW_HEIGHT;

            drawMailIcon(graphics, entry, 11, rowY + 8);

            graphics.text(font, Component.literal(entry.title()), 35, rowY + 2,
                    0xFFFFFFFF, false);

            Component preview = getMailPreview(entry);

            List<FormattedCharSequence> lines = font.split(preview, MAIL_ROW_WIDTH - 32);

            for (int line = 0; line < Math.min(2, lines.size()); line++) {
                graphics.text(font, lines.get(line), 35, rowY + 12 + line * 10,
                        0xFF000000, false);
            }
        }
    }

    private void extractMailDetailLabels(GuiGraphicsExtractor graphics) {
        ComputerMailEntry entry = getSelectedMail();

        if (entry == null) {
            return;
        }

        var task = entry.task();

        graphics.text(font, Component.literal(task.name()), 14, 12, 0xFFFFFFFF, false);

        graphics.text(font, Component.literal("From: " + task.contractor()), 14, 27,
                0xFF80CBC4, false);

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

            graphics.text(font, Component.literal(requirement.required() + "x " + requirement.label()),
                    20, y, 0xFFD7E1E5, false);

            y += 11;

            shown++;
        }

        graphics.text(font, Component.literal("Rewards: +" + task.experienceReward() + " XP, +" +
                task.moneyReward() + " money"), 14, 139, 0xFF80CBC4, false);
    }

    private void extractMinazonLabels(GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.literal("Minazon"), 14, 12,
                0xFFFFFFFF, false);

        graphics.text(font, Component.literal("Balance: " + menu.balance()),
                14, 24, 0xFF80CBC4, false);

        if (menu.offers().isEmpty()) {
            graphics.centeredText(font, Component.literal("No offers available."),
                    imageWidth / 2, 85, 0xFF9EA7AA);
        }

        graphics.centeredText(font, Component.literal("Orders arrive the next Minecraft morning"),
                imageWidth / 2, 142, 0xFF9EA7AA);
    }

    private void drawDesktopIcon(GuiGraphicsExtractor graphics, int x, int y, int textureY, Component name) {
        boolean hovered = isDesktopIconHovered(x, y);

        if (hovered) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, DESKTOP_ICONS, x, y, 32, textureY,
                    ICON_SIZE, ICON_SIZE, 256, 256);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, DESKTOP_ICONS, x, y, 0, textureY,
                ICON_SIZE, ICON_SIZE, 256, 256);

        int center = x + ICON_SIZE / 2;

        graphics.text(font, name, center - font.width(name) / 2 + 1, y + ICON_SIZE + 2,
                0xFF404040, false);

        graphics.text(font, name, center - font.width(name) / 2, y + ICON_SIZE + 1,
                0xFFFFFFFF, false);
    }

    private void drawDesktopCount(GuiGraphicsExtractor graphics, int x, int y, long count) {
        if (count <= 0L) {
            return;
        }

        Component text = Component.literal(Long.toString(count));

        graphics.text(font, text, x + ICON_SIZE - 1 - font.width(text), y + 4,
                0xFFAA0000, false);
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

        return menu.jobMail().stream().filter(entry ->
                entry.emailId().equals(selectedMailId)).findFirst().orElse(null);
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
        ).bounds(leftPos + 15, topPos + 153, 90, 22).build());

        Button accept = this.addRenderableWidget(Button.builder(Component.literal(entry.canAccept()
                ? "Accept Job" : "Unavailable"), button -> {
            button.active = false;

            NetworkManager.sendToServer(new AcceptEmailJobPayload(entry.emailId()));
        }).bounds(leftPos + 131, topPos + 153, 110, 22).build());

        accept.active = entry.canAccept();
    }

    private void drawMailBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MAIL_TEXTURE, leftPos + 3,
                topPos + 3, 0, 0, 250, 188, 512, 512);

        List<ComputerInboxEntry> inbox = menu.inbox();

        for (int row = 0; row < MAILS_PER_PAGE; row++) {
            int index = mailOffset + row;

            if (index >= inbox.size()) {
                break;
            }

            ComputerInboxEntry entry = inbox.get(index);

            int rowY = MAIL_ROW_Y + row * MAIL_ROW_HEIGHT;

            boolean hovered = isInside(mouseX, mouseY, MAIL_ROW_X, rowY, MAIL_ROW_WIDTH, MAIL_ROW_HEIGHT);
            boolean read = isRead(entry);

            int textureY;

            if (read) {
                textureY = hovered ? 287 : 254;
            } else {
                textureY = hovered ? 221 : 188;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, MAIL_TEXTURE, leftPos + MAIL_ROW_X,
                    topPos + rowY, 0, textureY, MAIL_ROW_WIDTH, MAIL_ROW_HEIGHT, 512, 512);
        }

        drawMailScrollbar(graphics);

        if (isInside(mouseX, mouseY, MAIL_CLOSE_X, MAIL_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, MAIL_TEXTURE, leftPos + MAIL_CLOSE_X,
                    topPos + MAIL_CLOSE_Y, 0, 320, 9, 9, 512, 512);
        }
    }

    private void drawMailScrollbar(GuiGraphicsExtractor graphics) {
        int emailCount = menu.inbox().size();
        int scrollbarX = imageWidth - 13;

        if (emailCount > MAILS_PER_PAGE) {
            float travel = 165F - 27F;
            float percent = mailOffset / (float) (emailCount - MAILS_PER_PAGE);

            int scrollbarY = MAIL_ROW_Y + Math.round(travel * percent);

            graphics.blit(RenderPipelines.GUI_TEXTURED, MAIL_TEXTURE, leftPos + scrollbarX,
                    topPos + scrollbarY, 239, 188, 10, 27, 512, 512);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, MAIL_TEXTURE, leftPos + scrollbarX,
                    topPos + MAIL_ROW_Y, 239, 215, 10, 27, 512, 512);
        }
    }

    private Component getMailPreview(ComputerInboxEntry entry) {
        return switch (entry.type()) {
            case CONTRACT -> menu.mail().stream().filter(mail -> mail.emailId()
                    .equals(entry.emailId())).findFirst().map(mail ->
                    Component.literal(mail.task().description())).orElseGet(Component::empty);
            case JOB -> menu.jobMail().stream().filter(mail -> mail.emailId()
                    .equals(entry.emailId())).findFirst().map(mail ->
                    Component.literal(mail.job().description())).orElseGet(Component::empty);
            case OFFER -> {
                String item = menu.offers().stream().filter(offer -> offer.id()
                        .equals(entry.referenceId())).map(offer ->
                        offer.item().toString()).findFirst().orElse(entry.title());

                yield Component.literal(item + " is now available for you to buy on Minazon");
            }
        };
    }

    private void drawMailIcon(GuiGraphicsExtractor graphics, ComputerInboxEntry entry, int x, int y) {
        switch (entry.type()) {
            case OFFER -> graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_ICON, x, y, 0, 0,
                    16, 16, 16, 16);
            case CONTRACT -> menu.mail().stream().filter(mail -> mail.emailId()
                    .equals(entry.emailId())).findFirst().ifPresent(mail ->
                    drawContractorHead(graphics, mail.task().skin(), x, y, !mail.canAccept()));
            case JOB -> menu.jobMail().stream().filter(mail -> mail.emailId()
                    .equals(entry.emailId())).findFirst().ifPresent(mail ->
                    drawContractorHead(graphics, mail.job().skin(), x, y, false));
        }
    }

    private void drawContractorHead(GuiGraphicsExtractor graphics, String skinName, int x, int y, boolean accepted) {
        Identifier skin = ContractorSkinManager.getTexture(skinName);

        if (skin != null) {
            graphics.pose().pushMatrix();

            graphics.pose().translate(x, y);

            graphics.pose().scale(2F, 2F);

            graphics.blit(RenderPipelines.GUI_TEXTURED, skin, 0, 0, 8, 8, 8,
                    8, 64, 64);

            graphics.blit(RenderPipelines.GUI_TEXTURED, skin, 0, 0, 40, 8, 8,
                    8, 64, 64);

            graphics.pose().popMatrix();
        }

        if (accepted) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ACCEPTED_TASK, x + 4, y + 4, 0, 0,
                    16, 16, 16, 16);
        }
    }
}