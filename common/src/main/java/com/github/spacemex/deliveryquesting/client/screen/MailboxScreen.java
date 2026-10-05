package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.menu.MailboxMenu;
import com.github.spacemex.deliveryquesting.menu.entry.MailboxParcelEntry;
import com.github.spacemex.deliveryquesting.networking.packets.CollectParcelPayload;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class MailboxScreen extends AbstractContainerScreen<MailboxMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/mailbox.png");
    private static final int INBOX_X = 8;
    private static final int INBOX_Y = 46;

    public MailboxScreen(MailboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 159);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        for (int i = 0; i < menu.parcels().size(); i++) {
            MailboxParcelEntry parcel = menu.parcels().get(i);
            ItemStack icon = new ItemStack(parcel.contractEnvelope() ? ModItems.SEALED_ENVELOPE.get() : ModItems.SEALED_PARCEL.get());
            graphics.item(icon, leftPos + INBOX_X + i * 18, topPos + INBOX_Y);
        }
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double relativeX = event.x() - leftPos;
            double relativeY = event.y() - topPos;

            if (relativeY >= INBOX_Y && relativeY < INBOX_Y + 16) {
                for (int i = 0; i < menu.parcels().size(); i++) {
                    int x = INBOX_X + i * 18;

                    if (relativeX >= x && relativeX < x + 16) {
                        MailboxParcelEntry parcel = menu.parcels().get(i);
                        NetworkManager.sendToServer(new CollectParcelPayload(parcel.id()));
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
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