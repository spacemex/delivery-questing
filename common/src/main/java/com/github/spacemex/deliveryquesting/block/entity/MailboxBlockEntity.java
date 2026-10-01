package com.github.spacemex.deliveryquesting.block.entity;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.item.DeliveryContainerItem;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.TaskRuntimeManager;
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

import java.util.*;

public final class MailboxBlockEntity extends BlockEntity implements Container {
    public static final int OUTBOX_SIZE = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(OUTBOX_SIZE, ItemStack.EMPTY);

    @Nullable
    private UUID groupId;

    public MailboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAILBOX.get(), pos, state);
    }

    public Optional<UUID> groupId() {
        return Optional.ofNullable(groupId);
    }

    public boolean bindToGroup(UUID groupId) {
        if (this.groupId == null) {
            this.groupId = groupId;
            setChanged();
            return true;
        }

        return this.groupId.equals(groupId);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MailboxBlockEntity mailbox) {
        if (level.isClientSide()) {
            return;
        }

        if (mailbox.groupId == null) {
            return;
        }

        if (mailbox.isEmpty()) {
            return;
        }

        if (Math.floorMod(level.getOverworldClockTime(), 24000L) != 20L) {
            return;
        }

        if (level.getServer() == null) {
            return;
        }

        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(level.getServer());
        Optional<DeliveryGroup> optionalGroup = data.getGroup(mailbox.groupId);

        if (optionalGroup.isEmpty()) {
            return;
        }

        List<ItemStack> outgoing = mailbox.drainOutbox();
        TaskRuntimeManager.MailboxSubmissionResult result = TaskRuntimeManager.submitMailboxItems(data, optionalGroup.get(), outgoing);

        DeliveryQuesting.LOGGER.debug("Mailbox at {} submitted {} item(s), discarded {} item(s), completed {} task(s), and completed {} repeatable job(s)",
                pos, result.submitted(), result.discarded(), result.completedTasks().size(), result.completedJobs().size());
    }

    private List<ItemStack> drainOutbox() {
        List<ItemStack> outgoing = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ItemStack containerStack = items.get(i);

            if (containerStack.getItem() instanceof DeliveryContainerItem containerItem) {
                outgoing.addAll(containerItem.getContents(containerStack));
            }

            items.set(i, ItemStack.EMPTY);
        }

        setChanged();
        return outgoing;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, items);
        output.storeNullable("group_id", UUIDUtil.CODEC, groupId);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);

        ContainerHelper.loadAllItems(input, items);
        groupId = input.read("group_id", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public int getContainerSize() {
        return OUTBOX_SIZE;
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
    public @NonNull ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, count);

        if (!stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(items, slot);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(getMaxStackSize(stack));
        items.set(slot, stack);
        setChanged();
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

    public void rebindToGroup(UUID groupId) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof DeliveryContainerItem;
    }
}