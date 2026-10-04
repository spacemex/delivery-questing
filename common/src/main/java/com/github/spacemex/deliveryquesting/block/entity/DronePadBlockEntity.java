package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class DronePadBlockEntity extends BlockEntity implements Container {
    public static final int PAYLOAD_SLOTS = 1;
    private final NonNullList<ItemStack> items = NonNullList.withSize(PAYLOAD_SLOTS, ItemStack.EMPTY);
    @Nullable
    private UUID groupId;

    public DronePadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRONE_PAD.get(), pos, state);
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

    public boolean isSkyFree() {
        Level level = getLevel();

        if (level == null) {
            return false;
        }

        BlockPos pos = getBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = pos.getY() + 1; y <= level.getMaxY(); y++) {
            cursor.set(pos.getX(), y, pos.getZ());

            if (!level.isEmptyBlock(cursor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items, true);
        output.storeNullable("group_id", UUIDUtil.CODEC, groupId);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        groupId = input.read("group_id", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public int getContainerSize() {
        return PAYLOAD_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof CardboardBoxItem)) {
            return;
        }
        stack.limitSize(1);
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof CardboardBoxItem;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }
}