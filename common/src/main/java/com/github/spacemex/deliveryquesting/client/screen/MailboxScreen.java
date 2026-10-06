package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.MailboxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class MailboxScreen extends AbstractContainerScreen<MailboxMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/mailbox.png");

    public MailboxScreen(MailboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 159);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 6, 0xFF404040, false);

        Component nextEmptying =
                getNextEmptyingText();

        graphics.text(font, nextEmptying, (imageWidth - font.width(nextEmptying)) / 2, 20, 0xFF404040, false);
        Component inbox = Component.literal("Inbox");

        graphics.text(font, inbox, 42 - font.width(inbox) / 2, 35, 0xFF404040, false);

        Component outbox = Component.literal("Outbox");

        graphics.text(font, outbox, 132 - font.width(outbox) / 2, 35, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);
    }

    private long getNextEmptying() {
        if (minecraft.level == null) {
            return 0L;
        }

        long time = Math.floorMod(minecraft.level.getOverworldClockTime(), 24000L);
        if (time <= 20L) {
            time += 24000L;
        }

        return 24020L - time;
    }

    private Component getNextEmptyingText() {
        long remaining = getNextEmptying();
        long hours = remaining / 1000L;
        int minutes = (int) ((remaining % 1000L) * 0.06F);

        return Component.literal(String.format("Next emptying in %02d:%02d", hours, minutes));
    }
}