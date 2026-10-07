package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.ContractorSkinManager;
import com.github.spacemex.deliveryquesting.client.widget.TaskRequirementWidget;
import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.menu.entry.*;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailContractPayload;
import com.github.spacemex.deliveryquesting.networking.packets.AcceptEmailJobPayload;
import com.github.spacemex.deliveryquesting.networking.packets.BuyOfferPayload;
import com.github.spacemex.deliveryquesting.networking.packets.MarkEmailReadPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {

    private static final int MAILS_PER_PAGE = 5;
    private int mailOffset;

    private View view = View.DESKTOP;

    private final Set<UUID> readThisSession = new HashSet<>();

    private static final Identifier MINAZON_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/minazon.png");
    private static final Identifier CONFIRM_BUY_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/confirm_buy.png");

    private static final int OFFERS_PER_PAGE = 4;
    private int minazonOffset;

    private static final int MINAZON_ROW_X = 3;
    private static final int MINAZON_ROW_Y = 26;
    private static final int MINAZON_ROW_WIDTH = 239;
    private static final int MINAZON_ROW_HEIGHT = 33;
    private static final int MINAZON_CLOSE_X = 244;
    private static final int MINAZON_CLOSE_Y = 3;
    private static final int CONFIRM_X = 56;
    private static final int CONFIRM_Y = 132;
    private static final int CANCEL_X = 147;
    private static final int CANCEL_Y = 132;
    private static final int CONFIRM_BUTTON_WIDTH = 53;
    private static final int CONFIRM_BUTTON_HEIGHT = 18;
    private static final int CONFIRM_CLOSE_X = 244;
    private static final int CONFIRM_CLOSE_Y = 3;
    private static final int CONFIRM_DIALOG_CLOSE_X = 194;
    private static final int CONFIRM_DIALOG_CLOSE_Y = 64;

    private Identifier selectedOfferId;

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

    private static final Identifier COMPUTER_CONTRACT_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/contract.png");
    private static final Identifier COMPUTER_TASK_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/computer_task.png");

    private static final int COMPUTER_DETAIL_CLOSE_X = 244;
    private static final int COMPUTER_DETAIL_CLOSE_Y = 3;

    private TaskRequirementWidget computerTaskWidget;

    private PlayerModel computerContractorModel;

    private Identifier computerContractorSkin;

    private static final Identifier GENERIC_MAIL_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/computer/generic_mail.png");

    private static final int OFFER_MAIL_CLOSE_X = 244;
    private static final int OFFER_MAIL_CLOSE_Y = 3;
    private static final int OFFER_MAIL_ITEM_X = 120;
    private static final int OFFER_MAIL_ITEM_Y = 128;
    private View minazonParent = View.DESKTOP;

    private enum View {
        DESKTOP,
        MAIL_LIST,
        MAIL_DETAIL,
        JOB_DETAIL,
        OFFER_DETAIL,
        MINAZON,
        CONFIRM_BUY
    }

    public ComputerScreen(ComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 194);
    }

    @Override
    protected void init() {
        super.init();

        computerContractorModel = new PlayerModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER),
                false);

        rebuildView();
    }

    private void rebuildView() {
        clearWidgets();

        computerTaskWidget = null;

        computerContractorSkin = null;

        switch (view) {
            case DESKTOP -> buildDesktop();
            case MAIL_LIST -> buildMailList();
            case MAIL_DETAIL -> buildMailDetail();
            case JOB_DETAIL -> buildJobDetail();
            case OFFER_DETAIL -> buildOfferDetail();
            case MINAZON -> buildMinazon();
            case CONFIRM_BUY -> buildConfirmBuy();
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

        BulletinBoardTaskEntry task = entry.task();

        prepareComputerTask(task);

        Button accept = addRenderableWidget(Button.builder(Component.literal(entry.canAccept() ?
                "Accept" : "Already Handled"), button -> {
            button.active = false;

            NetworkManager.sendToServer(new AcceptEmailContractPayload(entry.emailId()));
        }).bounds(leftPos + 67, topPos + 70, 68, 20).build());

        accept.active = entry.canAccept();
    }

    private void buildMinazon() {

    }

    private void buildConfirmBuy() {
        if (getSelectedOffer() == null) {
            view = View.MINAZON;

            rebuildView();
        }
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

                selectedOfferId = entry.referenceId();

                view = View.OFFER_DETAIL;
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

        if (view == View.MAIL_DETAIL || view == View.JOB_DETAIL) {
            drawComputerTaskBackground(graphics, mouseX - leftPos, mouseY - topPos);

            return;
        }

        if (view == View.OFFER_DETAIL) {
            drawOfferMailBackground(graphics, mouseX - leftPos, mouseY - topPos);

            return;
        }

        if (view == View.MINAZON) {
            drawMinazonBackground(graphics, mouseX - leftPos, mouseY - topPos);

            return;
        }

        if (view == View.CONFIRM_BUY) {
            drawConfirmBuyBackground(graphics, mouseX - leftPos, mouseY - topPos);

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
            case OFFER_DETAIL -> extractOfferMailLabels(graphics);
            case MINAZON -> extractMinazonLabels(graphics);
            case CONFIRM_BUY -> extractConfirmBuyLabels(graphics);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x() - leftPos;
            double mouseY = event.y() - topPos;

            if (view == View.DESKTOP) {
                if (isInside(mouseX, mouseY, MINTERNET_X, MINTERNET_Y, ICON_SIZE, ICON_SIZE)) {
                    minazonParent = View.DESKTOP;

                    selectedOfferId = null;

                    view = View.MINAZON;

                    minazonOffset = 0;

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

            if (view == View.MAIL_DETAIL || view == View.JOB_DETAIL) {
                if (isInside(mouseX, mouseY, COMPUTER_DETAIL_CLOSE_X, COMPUTER_DETAIL_CLOSE_Y, 9, 9)) {
                    view = View.MAIL_LIST;

                    selectedMailId = null;

                    rebuildView();

                    return true;
                }

                if (computerTaskWidget != null && computerTaskWidget.mouseClicked(mouseX, mouseY)) {
                    return true;
                }
            }

            if (view == View.MINAZON) {
                if (isInside(mouseX, mouseY, MINAZON_CLOSE_X, MINAZON_CLOSE_Y, 9, 9)) {
                    view = minazonParent;

                    if (minazonParent == View.DESKTOP) {
                        selectedOfferId = null;
                    }

                    rebuildView();

                    return true;
                }

                List<ComputerOfferEntry> offers = menu.offers();

                for (int row = 0; row < OFFERS_PER_PAGE; row++) {
                    int index = minazonOffset + row;

                    if (index >= offers.size()) {
                        break;
                    }

                    int rowY = MINAZON_ROW_Y + row * MINAZON_ROW_HEIGHT;

                    if (!isInside(mouseX, mouseY, MINAZON_ROW_X, rowY, MINAZON_ROW_WIDTH, MINAZON_ROW_HEIGHT)) {
                        continue;
                    }

                    ComputerOfferEntry offer = offers.get(index);

                    if (!offer.canBuy()) {
                        return true;
                    }

                    selectedOfferId = offer.id();

                    view = View.CONFIRM_BUY;

                    rebuildView();

                    return true;
                }
            }

            if (view == View.CONFIRM_BUY) {
                ComputerOfferEntry offer = getSelectedOffer();

                if (offer == null) {
                    view = View.MINAZON;

                    rebuildView();

                    return true;
                }

                if (isInside(mouseX, mouseY, CONFIRM_X, CONFIRM_Y, CONFIRM_BUTTON_WIDTH, CONFIRM_BUTTON_HEIGHT)) {
                    NetworkManager.sendToServer(new BuyOfferPayload(offer.id()));

                    return true;
                }

                if (isInside(mouseX, mouseY, CANCEL_X, CANCEL_Y, CONFIRM_BUTTON_WIDTH, CONFIRM_BUTTON_HEIGHT)
                        || isInside(mouseX, mouseY, CONFIRM_DIALOG_CLOSE_X, CONFIRM_DIALOG_CLOSE_Y, 9, 9)) {
                    view = View.MINAZON;

                    rebuildView();

                    return true;
                }

                if (isInside(mouseX, mouseY, CONFIRM_CLOSE_X, CONFIRM_CLOSE_Y, 9, 9)) {
                    selectedOfferId = null;

                    view = View.DESKTOP;

                    rebuildView();

                    return true;
                }
            }

            if (view == View.OFFER_DETAIL) {
                if (isInside(mouseX, mouseY, OFFER_MAIL_CLOSE_X, OFFER_MAIL_CLOSE_Y, 9, 9)) {
                    selectedOfferId = null;

                    view = View.MAIL_LIST;

                    rebuildView();

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

        if (view == View.MINAZON) {
            int maxOffset = Math.max(0, menu.offers().size() - OFFERS_PER_PAGE);

            if (maxOffset > 0) {
                if (scrollY < 0D) {
                    minazonOffset = Math.min(minazonOffset + 1, maxOffset);
                } else if (scrollY > 0D) {
                    minazonOffset = Math.max(minazonOffset - 1, 0);
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

        drawComputerTaskDetail(graphics, entry.job(), true);
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

        drawComputerTaskDetail(graphics, entry.task(), false);
    }

    private void extractMinazonLabels(GuiGraphicsExtractor graphics) {
        int balanceX = 5 - leftPos;
        int balanceY = 5 - topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, balanceX, balanceY, 0, 287,
                56, 32, 512, 512);

        Component balanceTitle = Component.literal("Balance");

        graphics.text(font, balanceTitle, balanceX + 28 - font.width(balanceTitle) / 2, balanceY + 6,
                0xFF404040, false);

        Component balance = Component.literal("$ " + formatMoney(menu.balance()));

        graphics.text(font, balance, balanceX + 28 - font.width(balance) / 2, balanceY + 18,
                0xFF008000, false);

        graphics.text(font, Component.literal("Minazon"), 5, 4, 0xFFFFFFFF, false);

        graphics.text(font, Component.literal("mc://minazon.com/"), 5, 16, 0xFF000000, false);

        List<ComputerOfferEntry> offers = menu.offers();

        for (int row = 0; row < OFFERS_PER_PAGE; row++) {
            int index = minazonOffset + row;

            if (index >= offers.size()) {
                break;
            }

            ComputerOfferEntry offer = offers.get(index);

            int rowY = MINAZON_ROW_Y + row * MINAZON_ROW_HEIGHT;

            ItemStack stack = createOfferStack(offer);

            if (!stack.isEmpty()) {
                graphics.item(stack, 10, rowY + 8);

                if (offer.count() > 1) {
                    String count = Integer.toString(offer.count());

                    graphics.text(font, Component.literal(count), 25 - font.width(count), rowY + 17,
                            0xFFFFFFFF, true);
                }
            }

            Component price = Component.literal(offer.price() + " $");

            int priceColor = offer.affordable() ? 0xFFFFFFFF : 0xFFAA0000;

            graphics.text(font, price, 35, rowY + 13, priceColor, false);

            if (!offer.unlocked()) {
                Component level = Component.literal("Requires level " + offer.minLevel());

                graphics.text(font, level, imageWidth - font.width(level) - 22, rowY + 13,
                        0xFFAA0000, false);
            }

            if (!stack.isEmpty() && isInside(desktopMouseX, desktopMouseY, MINAZON_ROW_X, rowY,
                    MINAZON_ROW_WIDTH, MINAZON_ROW_HEIGHT)) {

                graphics.setTooltipForNextFrame(font, stack, desktopMouseX + leftPos, desktopMouseY + topPos);
            }
        }
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

    private int findOfferOffset(Identifier offerId) {
        List<ComputerOfferEntry> offers = menu.offers();

        for (int i = 0; i < offers.size(); i++) {
            if (!offers.get(i).id().equals(offerId)) {
                continue;
            }

            int maxOffset = Math.max(0, offers.size() - OFFERS_PER_PAGE);

            return Math.min(i, maxOffset);
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

        BulletinBoardTaskEntry job = entry.job();

        prepareComputerTask(job);

        Button accept = addRenderableWidget(Button.builder(Component.literal(entry.canAccept() ?
                        "Accept" : "Unavailable"), button -> {
                    button.active = false;

                    NetworkManager.sendToServer(new AcceptEmailJobPayload(entry.emailId()));
                })
                .bounds(leftPos + 67, topPos + 70, 68, 20).build());

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
                Component item = menu.offers().stream()
                        .filter(offer -> offer.id().equals(entry.referenceId()))
                        .findFirst()
                        .map(offer -> getItemDisplayName(offer.item()))
                        .orElseGet(() -> Component.literal(entry.title()));

                yield Component.empty()
                        .append(item).append(Component.literal(" is now available for you to buy on Minazon"));
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

    private void prepareComputerTask(BulletinBoardTaskEntry task) {
        computerTaskWidget = new TaskRequirementWidget(144, 15, task.requirements(),
                false, COMPUTER_TASK_TEXTURE);

        computerContractorSkin = ContractorSkinManager.getTexture(task.skin());
    }

    private void drawComputerTaskBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, COMPUTER_CONTRACT_TEXTURE,
                leftPos + 3, topPos + 3, 0, 0, 250, 188, 256, 256);

        if (isInside(mouseX, mouseY, COMPUTER_DETAIL_CLOSE_X, COMPUTER_DETAIL_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, COMPUTER_CONTRACT_TEXTURE,
                    leftPos + COMPUTER_DETAIL_CLOSE_X, topPos + COMPUTER_DETAIL_CLOSE_Y,
                    0, 188, 9, 9, 256, 256);
        }
    }

    private void drawComputerTaskDetail(GuiGraphicsExtractor graphics, BulletinBoardTaskEntry task, boolean repeatable) {
        graphics.text(font, Component.literal(task.name()), 5, 4, 0xFFFFFFFF, false);

        drawComputerContractor(graphics);

        graphics.text(font, Component.literal(task.profession()), 6, 96, 0xFF000000, false);

        Component rewards = Component.literal("Rewards");

        graphics.text(font, rewards, 101 - font.width(rewards) / 2, 15, 0xFF000000, false);

        graphics.text(font, Component.literal("XP: +" + task.experienceReward()), 63, 25,
                0xFF404040, false);

        graphics.text(font, Component.literal("Money: +" + task.moneyReward()), 63, 35,
                0xFF404040, false);

        if (repeatable) {
            graphics.text(font, Component.literal("Repeatable Job"), 63, 46,
                    0xFF8A6500, false);
        }

        Component name = Component.literal(task.name());

        graphics.text(font, name, (imageWidth - font.width(name)) / 2, 123, 0xFF000000, false);

        List<FormattedCharSequence> description = font.split(Component.literal(task.description()),
                imageWidth - 16);

        int y = 136;

        for (FormattedCharSequence line : description) {
            if (y > 181) {
                break;
            }

            graphics.text(font, line, 8, y, 0xFF404040, false);

            y += font.lineHeight + 1;
        }

        if (computerTaskWidget != null) {
            computerTaskWidget.extract(graphics, desktopMouseX, desktopMouseY,
                    desktopMouseX + leftPos, desktopMouseY + topPos);
        }
    }

    private void drawComputerContractor(GuiGraphicsExtractor graphics) {
        if (computerContractorSkin == null || computerContractorModel == null) {
            return;
        }

        int portraitWidth = 46;
        int portraitHeight = 73;

        int leftPadding = 8;
        int topPadding = 15;

        int x0 = leftPos + leftPadding;
        int y0 = topPos + topPadding;
        int x1 = x0 + portraitWidth;
        int y1 = y0 + portraitHeight;

        float scale = 0.90F * (y1 - y0) / 2.125F;

        graphics.skin(computerContractorModel, computerContractorSkin, scale,
                0F, 0F, -1.0625F, x0, y0, x1, y1);
    }

    private ItemStack createOfferStack(ComputerOfferEntry offer) {
        Item item = BuiltInRegistries.ITEM.getValue(offer.item());

        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, Math.max(1, offer.count()));
    }

    private ComputerOfferEntry getSelectedOffer() {
        if (selectedOfferId == null) {
            return null;
        }

        return menu.offers().stream().filter(offer -> offer.id()
                .equals(selectedOfferId)).findFirst().orElse(null);
    }

    private void drawMinazonBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, leftPos + 3, topPos + 3,
                0, 0, 250, 188, 512, 512);

        List<ComputerOfferEntry> offers = menu.offers();

        for (int row = 0; row < OFFERS_PER_PAGE; row++) {
            int index = minazonOffset + row;

            if (index >= offers.size()) {
                break;
            }

            ComputerOfferEntry offer = offers.get(index);

            int rowY = MINAZON_ROW_Y + row * MINAZON_ROW_HEIGHT;

            boolean hovered = isInside(mouseX, mouseY, MINAZON_ROW_X, rowY, MINAZON_ROW_WIDTH, MINAZON_ROW_HEIGHT);

            int textureY;

            if (!offer.unlocked()) {
                textureY = 254;
            } else if (hovered) {
                textureY = 221;
            } else {
                textureY = 188;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, leftPos + MINAZON_ROW_X,
                    topPos + rowY, 0, textureY, MINAZON_ROW_WIDTH, MINAZON_ROW_HEIGHT,
                    512, 512);
        }

        drawMinazonScrollbar(graphics);

        if (isInside(mouseX, mouseY, MINAZON_CLOSE_X, MINAZON_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, leftPos + MINAZON_CLOSE_X,
                    topPos + MINAZON_CLOSE_Y, 0, 319, 9, 9, 512, 512);
        }
    }

    private void drawMinazonScrollbar(GuiGraphicsExtractor graphics) {
        int offerCount = menu.offers().size();

        int scrollbarX = imageWidth - 13;

        if (offerCount > OFFERS_PER_PAGE) {
            float travel = 165F - 27F;
            float percent = minazonOffset / (float) (offerCount - OFFERS_PER_PAGE);

            int scrollbarY = MINAZON_ROW_Y + Math.round(travel * percent);

            graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, leftPos + scrollbarX,
                    topPos + scrollbarY, 239, 188, 10, 27, 512, 512);

        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, MINAZON_TEXTURE, leftPos + scrollbarX,
                    topPos + MINAZON_ROW_Y, 239, 215, 10, 27, 512, 512);
        }
    }

    private void drawConfirmBuyBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONFIRM_BUY_TEXTURE, leftPos + 3,
                topPos + 3, 0, 0, 250, 188, 256, 256);

        drawConfirmButtonBackground(graphics, mouseX, mouseY, CONFIRM_X, CONFIRM_Y);

        drawConfirmButtonBackground(graphics, mouseX, mouseY, CANCEL_X, CANCEL_Y);

        if (isInside(mouseX, mouseY, CONFIRM_CLOSE_X, CONFIRM_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, CONFIRM_BUY_TEXTURE, leftPos + CONFIRM_CLOSE_X,
                    topPos + CONFIRM_CLOSE_Y, 0, 224, 9, 9, 256, 256);
        }

        if (isInside(mouseX, mouseY, CONFIRM_DIALOG_CLOSE_X, CONFIRM_DIALOG_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, CONFIRM_BUY_TEXTURE, leftPos + CONFIRM_DIALOG_CLOSE_X,
                    topPos + CONFIRM_DIALOG_CLOSE_Y, 0, 224, 9, 9, 256, 256);
        }
    }

    private void drawConfirmButtonBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y) {
        boolean hovered = isInside(mouseX, mouseY, x, y, CONFIRM_BUTTON_WIDTH, CONFIRM_BUTTON_HEIGHT);

        graphics.blit(RenderPipelines.GUI_TEXTURED, CONFIRM_BUY_TEXTURE, leftPos + x, topPos + y, 0,
                hovered ? 206 : 188, CONFIRM_BUTTON_WIDTH, CONFIRM_BUTTON_HEIGHT, 256, 256);
    }

    private void extractConfirmBuyLabels(GuiGraphicsExtractor graphics) {
        ComputerOfferEntry offer = getSelectedOffer();

        if (offer == null) {
            return;
        }

        ItemStack stack = createOfferStack(offer);

        Component itemName = stack.isEmpty() ? Component.literal(offer.item().toString()) : stack.getHoverName();

        graphics.text(font, Component.literal("mc://minazon.com/"), 5, 16,
                0xFF000000, false);

        graphics.text(font, Component.literal("mc://minazon.com/confirm"), 55, 77,
                0xFF000000, false);

        Component buy = Component.literal("Do you want to buy " + offer.count() + " x ").append(itemName)
                .append(Component.literal(" for " + offer.price() + " $?"));

        List<FormattedCharSequence> lines = font.split(buy, 144);

        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i), 56, 89 + i * 10, 0xFF000000, false);
        }

        Component confirm = Component.literal("Confirm");

        graphics.text(font, confirm, CONFIRM_X + CONFIRM_BUTTON_WIDTH / 2 - font.width(confirm) / 2,
                CONFIRM_Y + 5, 0xFF000000, false);

        Component cancel = Component.literal("Cancel");

        graphics.text(font, cancel, CANCEL_X + CONFIRM_BUTTON_WIDTH / 2 - font.width(cancel) / 2,
                CANCEL_Y + 5, 0xFF000000, false);
    }

    private static String formatMoney(long amount) {
        String[] suffixes = {
                "",
                "k",
                "M",
                "B",
                "T",
                "Qa",
                "Qi"
        };

        double value = amount;
        int suffixIndex = 0;

        while (value >= 1_000D && suffixIndex < suffixes.length - 1) {
            value /= 1_000D;
            suffixIndex++;
        }

        if (suffixIndex == 0) {
            return Long.toString(amount);
        }

        if (value >= 100D || value == Math.floor(value)) {
            return String.format("%.0f%s", value, suffixes[suffixIndex]);
        }

        return String.format("%.1f%s", value, suffixes[suffixIndex]);
    }

    private Component getItemDisplayName(Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);

        if (item == null || item == Items.AIR) {
            return Component.literal(itemId.toString());
        }

        return new ItemStack(item).getHoverName();
    }

    private void buildOfferDetail() {
        ComputerOfferEntry offer = getSelectedOffer();

        if (offer == null) {
            selectedOfferId = null;

            view = View.MAIL_LIST;

            rebuildView();

            return;
        }

        addRenderableWidget(Button.builder(Component.literal("View"), button -> {
                    minazonParent = View.OFFER_DETAIL;

                    minazonOffset = findOfferOffset(offer.id());

                    view = View.MINAZON;

                    rebuildView();
                })
                .bounds(leftPos + 78, topPos + 160, 100, 20).build());
    }

    private void drawOfferMailBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_MAIL_TEXTURE, leftPos + 3,
                topPos + 3, 0, 0, 250, 188, 256, 256);

        if (isInside(mouseX, mouseY, OFFER_MAIL_CLOSE_X, OFFER_MAIL_CLOSE_Y, 9, 9)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_MAIL_TEXTURE, leftPos + OFFER_MAIL_CLOSE_X,
                    topPos + OFFER_MAIL_CLOSE_Y, 0, 188, 9, 9, 256, 256);
        }
    }

    private void extractOfferMailLabels(GuiGraphicsExtractor graphics) {
        ComputerOfferEntry offer = getSelectedOffer();

        if (offer == null) {
            return;
        }

        ItemStack stack = createOfferStack(offer);

        Component itemName = getItemDisplayName(offer.item());

        Component title = Component.empty().append("Now available: ").append(itemName);

        graphics.text(font, title, 5, 4, 0xFFFFFFFF, false);

        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 15, 0xFF000000, false);

        Component description = Component.empty().append(itemName)
                .append(Component.literal(" is now available for you to buy on Minazon"));

        List<FormattedCharSequence> lines = font.split(description, imageWidth - 16);

        int y = 30;

        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, 11, y, 0xFF404040, false);

            y += 10;
        }

        if (!stack.isEmpty()) {
            graphics.item(stack, OFFER_MAIL_ITEM_X, OFFER_MAIL_ITEM_Y);

            if (offer.count() > 1) {
                String count = Integer.toString(offer.count());

                graphics.text(font, Component.literal(count), OFFER_MAIL_ITEM_X + 15 - font.width(count),
                        OFFER_MAIL_ITEM_Y + 9, 0xFFFFFFFF, true);
            }

            if (isInside(desktopMouseX, desktopMouseY, OFFER_MAIL_ITEM_X, OFFER_MAIL_ITEM_Y, 16, 16)) {
                graphics.setTooltipForNextFrame(font, stack, desktopMouseX + leftPos, desktopMouseY + topPos);
            }
        }
    }
}