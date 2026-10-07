package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.menu.DronePadMenu;
import com.github.spacemex.deliveryquesting.networking.packets.SubmitDroneDeliveryPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class DronePadScreen extends AbstractContainerScreen<DronePadMenu> {

    private Button sendButton;

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/gui/container/drone_pad.png");
    private static final Identifier UPGRADE_SLOT_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/item/upgrade_slot.png");

    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 53;

    public DronePadScreen(DronePadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();

        sendButton = addRenderableWidget(Button.builder(Component.literal("Send"), button -> {
            button.active = false;

            NetworkManager.sendToServer(new SubmitDroneDeliveryPayload(menu.blockPos()));
        }).bounds(leftPos + 110, topPos + 59, 50, 20).build());

        sendButton.active = menu.hasPayload();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (sendButton != null) {
            sendButton.active = menu.hasPayload() && menu.isDroneFullyCharged();
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth,
                imageHeight, 256, 256);

        drawEnergyBar(graphics, leftPos + 27, topPos + 17, menu.padEnergy(), DronePadBlockEntity.ENERGY_CAPACITY);


        if (menu.droneEnergy() >= 0) {
            drawEnergyBar(graphics, leftPos + 133, topPos + 17, menu.droneEnergy(), DroneEntity.ENERGY_CAPACITY);
        }

        if (!menu.getSlot(DronePadBlockEntity.UPGRADE_SLOT).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT_TEXTURE, leftPos + 80, topPos + 59,
                    0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 26, 7, 0xFF404040, false);

        Component droneTitle = Component.translatable("entity.delivery_questing.drone");

        graphics.text(font, droneTitle, imageWidth - 26 - font.width(droneTitle), 7,
                0xFF404040, false);

        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);
    }

    private void drawEnergyBar(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        if (capacity <= 0 || energy <= 0) {
            return;
        }

        int filled = Math.round((energy / (float) capacity) * BAR_HEIGHT);

        filled = Math.max(0, Math.min(BAR_HEIGHT, filled));

        if (filled <= 0) {
            return;
        }

        int missing = BAR_HEIGHT - filled;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + missing,
                176, missing, BAR_WIDTH, filled, 256, 256);
    }
}