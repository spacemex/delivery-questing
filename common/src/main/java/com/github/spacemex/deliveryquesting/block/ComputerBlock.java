package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.ComputerBlockEntity;
import com.github.spacemex.deliveryquesting.menu.ComputerMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ComputerBlock extends BaseEntityBlock {

    public ComputerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ComputerBlock::new);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) {
            return;
        }

        if (!(placer instanceof ServerPlayer player)) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof ComputerBlockEntity computer)) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        data.getGroupForPlayer(player.getUUID()).ifPresent(group -> computer.bindToGroup(group.id()));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ComputerBlockEntity computer) {
            ComputerMenu.open(serverPlayer, pos, computer);
        }

        return InteractionResult.SUCCESS;
    }
}