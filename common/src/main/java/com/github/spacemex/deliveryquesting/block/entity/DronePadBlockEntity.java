package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.github.spacemex.deliveryquesting.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
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
    @Nullable
    private UUID droneId;

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

    public Optional<DroneEntity> getDrone() {
        Level level = getLevel();

        if (level == null || droneId == null) {
            return Optional.empty();
        }

        Entity entity = level.getEntity(droneId);

        if (entity instanceof DroneEntity drone) {
            return Optional.of(drone);
        }
        return Optional.empty();
    }

    public Optional<UUID> droneId() {
        return Optional.ofNullable(droneId);
    }

    public Optional<DroneEntity> getOrCreateDrone() {
        Level level = getLevel();

        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        Optional<DroneEntity> existing = getDrone();

        if (existing.isPresent()) {
            return existing;
        }

        if (!isSkyFree()) {
            return Optional.empty();
        }

        DroneEntity drone = ModEntities.DRONE.get().create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);

        if (drone == null) {
            return Optional.empty();
        }

        drone.initialize(getBlockPos());

        if (!serverLevel.addFreshEntity(drone)) {
            return Optional.empty();
        }

        droneId = drone.getUUID();
        setChanged();
        return Optional.of(drone);
    }

    public void rebindToGroup(UUID groupId) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DronePadBlockEntity pad) {
        if (level.isClientSide()) {
            return;
        }

        if (pad.groupId == null) {
            return;
        }

        if (pad.getDrone().isEmpty() && pad.isSkyFree()) {
            pad.getOrCreateDrone();
        }
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
        output.storeNullable("drone_id", UUIDUtil.CODEC, droneId);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        groupId = input.read("group_id", UUIDUtil.CODEC).orElse(null);
        droneId = input.read("drone_id", UUIDUtil.CODEC).orElse(null);
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