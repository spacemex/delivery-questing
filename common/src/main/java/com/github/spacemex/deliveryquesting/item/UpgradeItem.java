package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.item.tier.UpgradeTier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

public final class UpgradeItem extends Item {

    private final UpgradeTier tier;

    public UpgradeItem(UpgradeTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public UpgradeTier tier() {
        return tier;
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        if (!(blockEntity instanceof DronePadBlockEntity) && !(blockEntity instanceof EnergyLiquifierBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean installed;

        if (blockEntity instanceof DronePadBlockEntity pad) {
            installed = pad.installUpgrade(context.getItemInHand(), context.getPlayer());
        } else {
            EnergyLiquifierBlockEntity liquifier = (EnergyLiquifierBlockEntity) blockEntity;

            installed = liquifier.installUpgrade(context.getItemInHand(), context.getPlayer());
        }

        return installed ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }
}