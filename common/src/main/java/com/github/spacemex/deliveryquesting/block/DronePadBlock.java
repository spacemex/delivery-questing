package com.github.spacemex.deliveryquesting.block;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.menu.DronePadMenu;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.Optional;

public final class DronePadBlock extends BaseEntityBlock {

    public DronePadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DronePadBlock::new);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new DronePadBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide() || !(placer instanceof ServerPlayer player) || !(level.getBlockEntity(pos) instanceof DronePadBlockEntity pad)) {
            return;
        }
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());
        data.getGroupForPlayer(player.getUUID()).ifPresent(group -> pad.bindToGroup(group.id()));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(level.getBlockEntity(pos) instanceof DronePadBlockEntity pad)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(serverPlayer.level().getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(serverPlayer.getUUID());

        if (optionalGroup.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.no_group"));
            return InteractionResult.SUCCESS_SERVER;
        }

        DeliveryGroup group = optionalGroup.get();
        if (pad.groupId().isEmpty()) {
            pad.bindToGroup(group.id());
        }

        if (!pad.groupId().filter(group.id()::equals).isPresent()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.wrong_group"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!pad.isSkyFree()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.delivery_questing.drone_pad.no_sky"));
            return InteractionResult.SUCCESS_SERVER;
        }
        DronePadMenu.open(serverPlayer, pos, pad);
        return InteractionResult.SUCCESS_SERVER;
    }
}