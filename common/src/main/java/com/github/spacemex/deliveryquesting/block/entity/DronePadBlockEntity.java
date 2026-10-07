package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.item.UpgradeItem;
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
import net.minecraft.world.inventory.ContainerData;
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
    @Nullable
    private UUID groupId;
    @Nullable
    private UUID droneId;
    public static final int PAYLOAD_SLOT = 0;
    public static final int UPGRADE_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int ENERGY_CAPACITY = 16_000;
    public static final int DRONE_CHARGE_RATE = 2;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int energy;
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> getDrone().filter(DroneEntity::isIdle).map(DroneEntity::getEnergy).orElse(-1);
                case 2 -> getDrone().filter(DroneEntity::isIdle).map(DroneEntity::getTier).orElse(getUpgradeLevel());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

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

    public ContainerData menuData() {
        return menuData;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(ENERGY_CAPACITY, energy));
        setChanged();
    }

    public int receiveEnergy(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int accepted = Math.min(amount, ENERGY_CAPACITY - energy);

        if (!simulate && accepted > 0) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    private int useEnergy(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int used = Math.min(energy, amount);

        if (used > 0) {
            energy -= used;
            setChanged();
        }
        return used;
    }

    public int getUpgradeLevel() {
        ItemStack stack = items.get(UPGRADE_SLOT);

        if (stack.getItem() instanceof UpgradeItem upgrade) {
            return upgrade.tier().level();
        }
        return 0;
    }

    public boolean installUpgrade(ItemStack source, @Nullable Player player) {
        if (!(source.getItem() instanceof UpgradeItem)) {
            return false;
        }

        if (!items.get(UPGRADE_SLOT).isEmpty()) {
            return false;
        }

        items.set(UPGRADE_SLOT, source.copyWithCount(1));

        if (player == null || !player.isCreative()) {
            source.shrink(1);
        }

        setChanged();
        return true;
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

        Optional<DroneEntity> optionalDrone = pad.getDrone();

        if (optionalDrone.isEmpty()) {
            if (pad.isSkyFree()) {
                pad.getOrCreateDrone();
            }

            return;
        }

        DroneEntity drone = optionalDrone.get();

        if (!drone.isIdle()) {
            return;
        }

        int tier = pad.getUpgradeLevel();

        if (drone.getTier() != tier) {
            drone.setTier(tier);
        }

        if (drone.getPayload().isEmpty()) {
            ItemStack waitingPayload = pad.getItem(PAYLOAD_SLOT);

            if (!waitingPayload.isEmpty() && waitingPayload.getItem() instanceof CardboardBoxItem) {
                ItemStack payload = pad.removeItemNoUpdate(PAYLOAD_SLOT);

                if (!drone.loadPayload(payload)) {
                    pad.setItem(PAYLOAD_SLOT, payload);
                }
            }
        }

        int missing = DroneEntity.ENERGY_CAPACITY - drone.getEnergy();

        if (missing > 0 && pad.energy > 0) {
            int requested = Math.min(DRONE_CHARGE_RATE, missing);
            int transferred = pad.useEnergy(requested);

            if (transferred > 0) {
                drone.addEnergy(transferred);
            }
        }

        if (!drone.getPayload().isEmpty() && drone.isFullyCharged()) {
            drone.launch();
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
        output.putInt("energy", energy);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        groupId = input.read("group_id", UUIDUtil.CODEC).orElse(null);
        droneId = input.read("drone_id", UUIDUtil.CODEC).orElse(null);
        energy = Math.max(0, Math.min(ENERGY_CAPACITY, input.getIntOr("energy", 0)));
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
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
        if (!stack.isEmpty()) {
            if (slot == PAYLOAD_SLOT && !(stack.getItem() instanceof CardboardBoxItem)) {
                return;
            }

            if (slot == UPGRADE_SLOT && !(stack.getItem() instanceof UpgradeItem)) {
                return;
            }
        }
        stack.limitSize(1);
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return switch (slot) {
            case PAYLOAD_SLOT -> stack.getItem() instanceof CardboardBoxItem;
            case UPGRADE_SLOT -> stack.getItem() instanceof UpgradeItem;
            default -> false;
        };
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