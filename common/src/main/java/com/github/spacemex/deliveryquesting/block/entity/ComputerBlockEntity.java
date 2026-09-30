package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ComputerBlockEntity extends BlockEntity {
    @Nullable
    private UUID groupId;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER.get(), pos, state);
    }

    public Optional<UUID> groupId() {
        return Optional.ofNullable(groupId);
    }

    public boolean bindToGroup(UUID groupId) {
        Objects.requireNonNull(groupId, "groupId");

        if (this.groupId == null) {
            this.groupId = groupId;
            setChanged();

            return true;
        }

        return this.groupId.equals(groupId);
    }

    public void rebindToGroup(UUID groupId) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        setChanged();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        output.storeNullable("group_id", UUIDUtil.CODEC, groupId);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        groupId = input.read("group_id", UUIDUtil.CODEC).orElse(null);
    }
}