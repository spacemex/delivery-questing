package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.menu.BulletinBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public final class BulletinBoardBlock extends Block {

    public BulletinBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BulletinBoardMenu.open(serverPlayer, pos);
        }

        return InteractionResult.SUCCESS;
    }
}
