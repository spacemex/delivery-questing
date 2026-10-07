package com.github.spacemex.deliveryquesting.client.widget;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.entry.BulletinBoardRequirementEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class TaskRequirementWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/task.png");

    private static final int WIDTH = 106;
    private static final int HEIGHT = 104;
    private static final int PER_PAGE = 4;

    private final Minecraft minecraft;

    private final Font font;

    private final int x;
    private final int y;

    private final List<BulletinBoardRequirementEntry> requirements;

    private final boolean showProgress;

    private int page;

    @Nullable
    private final Runnable onInfoClick;

    public TaskRequirementWidget(int x, int y, List<BulletinBoardRequirementEntry> requirements, boolean showProgress,
                                 @Nullable Runnable onInfoClick) {
        this.x = x;

        this.y = y;

        this.requirements = List.copyOf(requirements);

        this.showProgress = showProgress;

        this.onInfoClick = onInfoClick;

        minecraft = Minecraft.getInstance();

        font = minecraft.font;
    }

    public TaskRequirementWidget(int x, int y, List<BulletinBoardRequirementEntry> requirements, boolean showProgress) {
        this(x, y, requirements, showProgress, null);
    }

    public void extract(GuiGraphicsExtractor graphics, int localMouseX, int localMouseY,
                        int screenMouseX, int screenMouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0,
                WIDTH, HEIGHT, 256, 256);

        drawTitle(graphics);

        drawInfoButton(graphics, localMouseX, localMouseY);

        drawRequirements(graphics, localMouseX, localMouseY, screenMouseX, screenMouseY);

        drawPageButtons(graphics, localMouseX, localMouseY);
    }

    private void drawTitle(GuiGraphicsExtractor graphics) {
        Component title = Component.literal("Requirements")
                .withStyle(ChatFormatting.DARK_GRAY);
        graphics.text(font, title, x + (WIDTH - font.width(title)) / 2, y + 4, 0xFF404040, false);
    }

    private void drawRequirements(GuiGraphicsExtractor graphics, int localMouseX, int localMouseY,
                                  int screenMouseX, int screenMouseY) {
        int first = page * PER_PAGE;
        int end = Math.min(first + PER_PAGE, requirements.size());
        int rowY = y + 15;

        for (int i = first; i < end; i++) {
            BulletinBoardRequirementEntry requirement = requirements.get(i);

            ItemStack stack = getDisplayStack(requirement);

            if (!stack.isEmpty()) {
                graphics.item(stack, x + 8, rowY);
            }

            Component amount = getAmountText(requirement);

            graphics.text(font, amount, x + 28, rowY + 5, getAmountColor(requirement), false);

            if (!stack.isEmpty() && isInside(localMouseX, localMouseY, x + 8, rowY, 16, 16)) {
                graphics.setTooltipForNextFrame(font, stack, screenMouseX, screenMouseY);
            }

            rowY += 18;
        }
    }

    private Component getAmountText(BulletinBoardRequirementEntry requirement) {
        if (!showProgress) {
            return Component.literal(formatAmount(requirement.required()));
        }

        return Component.literal(formatAmount(requirement.current()) + " / "
                + formatAmount(requirement.required()));
    }

    private int getAmountColor(BulletinBoardRequirementEntry requirement) {
        if (!showProgress) {
            return 0xFF404040;
        }

        if (requirement.current() <= 0L) {
            return 0xFF404040;
        }

        if (requirement.complete()) {
            return 0xFF006400;
        }

        return 0xFF8B0000;
    }

    private ItemStack getDisplayStack(BulletinBoardRequirementEntry requirement) {
        if (requirement.isItem()) {
            Item item = BuiltInRegistries.ITEM.getValue(requirement.target());

            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(item);
        }

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, requirement.target());

        Optional<HolderSet.Named<Item>> optional = BuiltInRegistries.ITEM.get(tagKey);

        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<Holder<Item>> values = optional.get().stream().toList();

        if (values.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int index = (int) ((Util.getMillis() / 1000L) % values.size());

        return new ItemStack(values.get(index).value());
    }

    private void drawPageButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (hasPrevious()) {
            boolean hovered = isPreviousHovered(mouseX, mouseY);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 10, y + HEIGHT - 15, hovered ? 124
                    : 106, 11, 18, 11, 256, 256);
        }

        if (hasNext()) {
            boolean hovered = isNextHovered(mouseX, mouseY);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + WIDTH - 35, y + HEIGHT - 15, hovered ? 124
                    : 106, 0, 18, 11, 256, 256);
        }
    }

    private void drawInfoButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (onInfoClick == null) {
            return;
        }

        boolean hovered = isInfoHovered(mouseX, mouseY);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + WIDTH - 13, y + 2, hovered ? 142
                : 153, 0, 11, 11, 256, 256);
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (onInfoClick != null && isInfoHovered(mouseX, mouseY)) {
            onInfoClick.run();

            return true;
        }

        if (hasPrevious() && isPreviousHovered(mouseX, mouseY)) {
            page = Math.max(0, page - 1);

            return true;
        }

        if (hasNext() && isNextHovered(mouseX, mouseY)) {
            page = Math.min(getPageCount() - 1, page + 1);

            return true;
        }

        return false;
    }

    private boolean hasPrevious() {
        return page > 0;
    }

    private boolean hasNext() {
        return page < getPageCount() - 1;
    }

    private int getPageCount() {
        return Math.max(1, (requirements.size() + PER_PAGE - 1) / PER_PAGE);
    }

    private boolean isPreviousHovered(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, x + 10, y + HEIGHT - 15, 18, 11);
    }

    private boolean isNextHovered(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, x + WIDTH - 35, y + HEIGHT - 15, 18, 11);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String formatAmount(long amount) {
        if (amount < 1_000L) {
            return Long.toString(amount);
        }

        if (amount < 1_000_000L) {
            return String.format("%.1f k", amount / 1_000F);
        }

        if (amount < 1_000_000_000L) {
            return String.format("%.1f M", amount / 1_000_000F);
        }

        return String.format("%.1f B", amount / 1_000_000_000F);
    }

    private boolean isInfoHovered(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, x + WIDTH - 13, y + 2, 11, 11);
    }
}