
package com.github.spacemex.deliveryquesting.client.screen;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.menu.EnergyLiquifierMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class EnergyLiquifierScreen
        extends AbstractContainerScreen<EnergyLiquifierMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DeliveryQuesting.MOD_ID,
                    "textures/gui/container/energy_liquifier.png");

    private static final Identifier UPGRADE_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DeliveryQuesting.MOD_ID,
                    "textures/item/upgrade_slot.png");

    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 53;

    private static final int ENERGY_X = 27;
    private static final int FLUID_X = 133;
    private static final int BAR_Y = 17;

    public EnergyLiquifierScreen(EnergyLiquifierMenu menu,
                                 Inventory inventory,
                                 Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(
                Component.literal("Swap"),
                button -> {
                    if (minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(
                                menu.containerId, 0);
                    }
                }
        ).bounds(
                leftPos + 74,
                topPos + 55,
                28, 20
        ).build());
    }

    @Override
    public void extractBackground(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX, int mouseY, float partialTick) {

        super.extractBackground(
                graphics, mouseX, mouseY, partialTick);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos, topPos,
                0, 0,
                imageWidth, imageHeight,
                256, 256
        );

        drawBar(
                graphics,
                leftPos + ENERGY_X,
                topPos + BAR_Y,
                menu.energy(),
                EnergyLiquifierBlockEntity.ENERGY_CAPACITY,
                200
        );

        drawBar(
                graphics,
                leftPos + FLUID_X,
                topPos + BAR_Y,
                menu.fluidAmount(),
                EnergyLiquifierBlockEntity.FLUID_CAPACITY,
                216
        );

        if (menu.isReversed()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + 76,
                    topPos + 36,
                    176, 0,
                    24, 15,
                    256, 256
            );
        }

        if (!menu.getSlot(
                EnergyLiquifierBlockEntity.UPGRADE_SLOT).hasItem()) {

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    UPGRADE_TEXTURE,
                    leftPos + 80,
                    topPos + 17,
                    0, 0,
                    16, 16,
                    16, 16
            );
        }
    }

    private void drawBar(GuiGraphicsExtractor graphics,
                         int x, int y,
                         int value, int capacity,
                         int sourceX) {

        if (capacity <= 0 || value <= 0) {
            return;
        }

        int filled = Math.clamp(
                Math.round(value / (float) capacity * BAR_HEIGHT),
                0,
                BAR_HEIGHT
        );

        if (filled <= 0) {
            return;
        }

        int missing = BAR_HEIGHT - filled;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y + missing,
                sourceX, missing,
                BAR_WIDTH, filled,
                256, 256
        );
    }

    @Override
    protected void extractLabels(
            GuiGraphicsExtractor graphics,
            int mouseX, int mouseY) {

        graphics.text(
                font,
                title,
                (imageWidth - font.width(title)) / 2,
                6,
                0xFF404040,
                false
        );

        graphics.text(
                font,
                playerInventoryTitle,
                8,
                imageHeight - 93,
                0xFF404040,
                false
        );
    }

    @Override
    protected void extractTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX, int mouseY) {

        super.extractTooltip(graphics, mouseX, mouseY);

        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;

        if (inside(relativeX, relativeY,
                ENERGY_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT)) {

            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(
                            menu.energy() + " / "
                                    + EnergyLiquifierBlockEntity.ENERGY_CAPACITY
                                    + " FE"),
                    mouseX, mouseY
            );

        } else if (inside(relativeX, relativeY,
                FLUID_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT)) {

            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(
                            "Liquid Energy: "
                                    + menu.fluidAmount() + " / "
                                    + EnergyLiquifierBlockEntity.FLUID_CAPACITY
                                    + " mB"),
                    mouseX, mouseY
            );

        } else if (inside(relativeX, relativeY,
                76, 36, 24, 15)) {

            String direction = menu.isReversed()
                    ? "Liquid Energy -> FE"
                    : "FE -> Liquid Energy";

            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(
                            direction + " (" + menu.conversionRate()
                                    + "/tick)"),
                    mouseX, mouseY
            );
        }
    }

    private boolean inside(int mouseX, int mouseY,
                           int x, int y,
                           int width, int height) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
