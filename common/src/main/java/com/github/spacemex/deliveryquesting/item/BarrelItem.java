
package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.item.tier.BarrelTier;
import com.github.spacemex.deliveryquesting.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class BarrelItem extends BlockItem {

    private final BarrelTier tier;

    public BarrelItem(Block block, BarrelTier tier, Properties properties) {
        super(block, properties);
        this.tier = tier;
    }

    public BarrelTier tier() {
        return tier;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NonNull Consumer<Component> builder, @NonNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        BarrelContents contents = stack.getOrDefault(ModDataComponents.BARREL_CONTENTS.get(), BarrelContents.EMPTY);

        String fluidName = contents.isEmpty() ? "Empty" : switch (contents.fluid().toString()) {
            case "minecraft:water" -> "Water";
            case "minecraft:lava" -> "Lava";
            default -> contents.fluid().toString();
        };

        builder.accept(Component.literal("Fluid: " + fluidName).withStyle(ChatFormatting.GRAY));

        builder.accept(Component.literal(
                contents.amount() + " / " + tier.capacity() + " mB").withStyle(ChatFormatting.GRAY));
    }
}
