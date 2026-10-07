package com.github.spacemex.deliveryquesting.entity;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DroneEntity extends Entity {

    private static final EntityDataAccessor<BlockPos> DATA_PAD_POS = SynchedEntityData
            .defineId(DroneEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData
            .defineId(DroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_PAYLOAD = SynchedEntityData
            .defineId(DroneEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final double IDLE_HEIGHT = 1.35D;
    private static final double ARRIVAL_DISTANCE_SQR = 0.04D;

    private static final EntityDataAccessor<Integer> DATA_ENERGY = SynchedEntityData
            .defineId(DroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData
            .defineId(DroneEntity.class, EntityDataSerializers.INT);

    public static final int ENERGY_CAPACITY = 16_000;
    private static final int LOADED_ENERGY_PER_TICK = 5;
    private static final int RETURN_ENERGY_PER_TICK = 1;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);

        noPhysics = true;

        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PAD_POS, BlockPos.ZERO);

        builder.define(DATA_STATE, FlightState.IDLE.id());

        builder.define(DATA_PAYLOAD, ItemStack.EMPTY);

        builder.define(DATA_ENERGY, 0);

        builder.define(DATA_TIER, 0);
    }

    public void initialize(BlockPos padPos) {
        setPadPos(padPos);

        setFlightState(FlightState.IDLE);

        setPayload(ItemStack.EMPTY);

        setEnergy(0);

        setTier(0);

        Vec3 idle = getIdlePosition();

        setPos(idle.x, idle.y, idle.z);
    }

    public int getEnergy() {
        return getEntityData().get(DATA_ENERGY);
    }

    public void setEnergy(int energy) {
        getEntityData().set(DATA_ENERGY, Math.max(0, Math.min(ENERGY_CAPACITY, energy)));
    }

    public void addEnergy(int amount) {
        if (amount <= 0) {
            return;
        }

        setEnergy(getEnergy() + amount);
    }

    public boolean isFullyCharged() {
        return getEnergy() >= ENERGY_CAPACITY;
    }

    public int getTier() {
        return getEntityData().get(DATA_TIER);
    }

    public void setTier(int tier) {
        getEntityData().set(DATA_TIER, Math.max(0, Math.min(6, tier)));
    }

    public int getPayloadTier() {
        ItemStack payload = getPayload();

        if (payload.getItem() instanceof CardboardBoxItem box) {
            return box.tier().level();
        }

        return 1;
    }

    public double getRiseSpeed() {
        return 0.1D * (getTier() + 1.0D) / getPayloadTier();
    }

    public BlockPos getPadPos() {
        return getEntityData().get(DATA_PAD_POS);
    }

    private void setPadPos(BlockPos pos) {
        getEntityData().set(DATA_PAD_POS, pos.immutable());
    }

    public FlightState getFlightState() {
        return FlightState.fromId(getEntityData().get(DATA_STATE));
    }

    private void setFlightState(FlightState state) {
        getEntityData().set(DATA_STATE, state.id());
    }

    public boolean isIdle() {
        return getFlightState() == FlightState.IDLE;
    }

    public ItemStack getPayload() {
        return getEntityData().get(DATA_PAYLOAD);
    }

    private void setPayload(ItemStack stack) {
        getEntityData().set(DATA_PAYLOAD, stack.copy());
    }

    public boolean launch(ItemStack payload) {
        if (!isIdle()) {
            return false;
        }

        if (!isFullyCharged()) {
            return false;
        }

        if (payload.isEmpty() || !(payload.getItem() instanceof CardboardBoxItem)) {
            return false;
        }

        if (!getPayload().isEmpty()) {
            return false;
        }

        setPayload(payload);

        setFlightState(FlightState.DEPARTING);

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        noPhysics = true;

        setNoGravity(true);

        if (level().isClientSide()) {
            return;
        }

        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos padPos = getPadPos();

        if (!(serverLevel.getBlockEntity(padPos) instanceof DronePadBlockEntity pad)) {
            dropPayload(serverLevel);

            discard();

            return;
        }

        switch (getFlightState()) {
            case IDLE -> tickIdle();
            case DEPARTING -> tickDeparting(serverLevel, pad);
            case RETURNING -> tickReturning();
        }
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        return false;
    }

    private void tickIdle() {
        moveToward(getIdlePosition(), 0.15D);
    }

    private void tickDeparting(ServerLevel level, DronePadBlockEntity pad) {
        if (getEnergy() <= 0) {
            crash(level);

            return;
        }

        setEnergy(getEnergy() - LOADED_ENERGY_PER_TICK);

        Vec3 target = new Vec3(getPadPos().getX() + 0.5D, level.getMaxY() + 8.0D, getPadPos().getZ() + 0.5D);

        if (!moveToward(target, getRiseSpeed())) {
            return;
        }

        submitPayload(level, pad);

        setFlightState(FlightState.RETURNING);
    }

    private void tickReturning() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }

        if (getEnergy() <= 0) {
            discard();

            return;
        }

        setEnergy(getEnergy() - RETURN_ENERGY_PER_TICK);

        if (!moveToward(getIdlePosition(), 1.0D)) {
            return;
        }

        setDeltaMovement(Vec3.ZERO);

        setFlightState(FlightState.IDLE);
    }

    private boolean moveToward(Vec3 target, double speed) {
        Vec3 difference = target.subtract(position());

        if (difference.lengthSqr() <= ARRIVAL_DISTANCE_SQR) {
            setPos(target.x, target.y, target.z);

            setDeltaMovement(Vec3.ZERO);

            return true;
        }

        Vec3 movement = difference.normalize().scale(Math.min(speed, difference.length()));

        setDeltaMovement(movement);

        move(MoverType.SELF, movement);

        return false;
    }

    private void crash(ServerLevel level) {
        DeliveryQuesting.LOGGER.debug("Drone at {} ran out of energy and crashed", blockPosition());

        dropPayload(level);

        discard();
    }

    private Vec3 getIdlePosition() {
        BlockPos padPos = getPadPos();

        return new Vec3(padPos.getX() + 0.5D, padPos.getY() + IDLE_HEIGHT, padPos.getZ() + 0.5D);
    }

    private void submitPayload(ServerLevel level, DronePadBlockEntity pad) {
        ItemStack payload = getPayload();

        if (!(payload.getItem() instanceof CardboardBoxItem boxItem)) {
            setPayload(ItemStack.EMPTY);

            return;
        }

        Optional<UUID> optionalGroupId = pad.groupId();

        if (optionalGroupId.isEmpty()) {
            returnPayload(level);

            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(level.getServer());

        Optional<DeliveryGroup> optionalGroup = data.getGroup(optionalGroupId.get());

        if (optionalGroup.isEmpty()) {
            returnPayload(level);

            return;
        }

        List<ItemStack> contents = boxItem.getContents(payload);

        if (contents.isEmpty()) {
            returnPayload(level);

            return;
        }

        DeliveryGroup group = optionalGroup.get();

        TaskRuntimeManager.MailboxSubmissionResult result = TaskRuntimeManager.submitDeliveryItems(data, group, contents);

        setPayload(ItemStack.EMPTY);

        Component message = Component.literal("Drone delivery completed: " + result.submitted()
                + " item(s) submitted, " + result.discarded() + " item(s) discarded.");

        for (UUID member : group.members()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(member);

            if (player != null) {
                player.sendSystemMessage(message);
            }
        }

        DeliveryQuesting.LOGGER.debug("Drone delivery for group '{}' submitted {} item(s), discarded {}",
                group.name(), result.submitted(), result.discarded());
    }

    private void returnPayload(ServerLevel level) {
        ItemStack payload = getPayload();

        if (payload.isEmpty()) {
            return;
        }

        BlockPos padPos = getPadPos();

        ItemEntity dropped = new ItemEntity(level, padPos.getX() + 0.5D, padPos.getY() + 1.0D,
                padPos.getZ() + 0.5D, payload.copy());

        dropped.setDefaultPickUpDelay();

        level.addFreshEntity(dropped);

        setPayload(ItemStack.EMPTY);
    }

    private void dropPayload(ServerLevel level) {
        ItemStack payload = getPayload();

        if (payload.isEmpty()) {
            return;
        }

        ItemEntity dropped = new ItemEntity(level, getX(), getY(), getZ(), payload.copy());

        dropped.setDefaultPickUpDelay();

        level.addFreshEntity(dropped);

        setPayload(ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        output.store("pad_pos", BlockPos.CODEC, getPadPos());

        output.putInt("flight_state", getFlightState().id());

        output.putInt("energy", getEnergy());

        output.putInt("tier", getTier());

        if (!getPayload().isEmpty()) {
            output.store("payload", ItemStack.CODEC, getPayload());
        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        input.read("pad_pos", BlockPos.CODEC).ifPresent(this::setPadPos);

        setFlightState(FlightState.fromId(input.getIntOr("flight_state", FlightState.IDLE.id())));

        setPayload(input.read("payload", ItemStack.CODEC).orElse(ItemStack.EMPTY));

        setEnergy(input.getIntOr("energy", 0));

        setTier(input.getIntOr("tier", 0));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @NonNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public enum FlightState {
        IDLE(0),
        DEPARTING(1),
        RETURNING(2);

        private final int id;

        FlightState(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static FlightState fromId(int id) {
            for (FlightState state : values()) {
                if (state.id == id) {
                    return state;
                }
            }

            return IDLE;
        }
    }
}