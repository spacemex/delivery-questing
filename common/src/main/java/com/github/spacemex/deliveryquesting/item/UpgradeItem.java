package com.github.spacemex.deliveryquesting.item;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.item.tier.UpgradeTier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
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
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DronePadBlockEntity pad)) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!pad.installUpgrade(context.getItemInHand(), context.getPlayer())) {
            return InteractionResult.PASS;
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}