
package com.github.spacemex.deliveryquesting.compat.energy.fabric;

import com.github.spacemex.deliveryquesting.block.EnergyLiquifierBlock;
import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class EnergyLiquifierEnergyCompat {

    private static final Map<EnergyLiquifierBlockEntity, Storage> STORAGES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EnergyLiquifierEnergyCompat() {
    }

    public static void register() {
        EnergyStorage.SIDED.registerForBlockEntity(
                (machine, side) -> {
                    Direction front = machine.getBlockState()
                            .getValue(EnergyLiquifierBlock.FACING);

                    if (side != null && side != front.getOpposite()) {
                        return null;
                    }

                    return STORAGES.computeIfAbsent(machine, Storage::new);
                },
                ModBlockEntities.ENERGY_LIQUIFIER.get()
        );
    }

    private static final class Storage
            extends SnapshotParticipant<Integer>
            implements EnergyStorage {

        private final WeakReference<EnergyLiquifierBlockEntity> reference;

        private Storage(EnergyLiquifierBlockEntity machine) {
            reference = new WeakReference<>(machine);
        }

        @Override
        public boolean supportsInsertion() {
            return reference.get() != null;
        }

        @Override
        public boolean supportsExtraction() {
            return reference.get() != null;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null) {
                return 0;
            }

            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int accepted = machine.receiveEnergy(requested, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.receiveEnergy(accepted, false);
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null) {
                return 0;
            }

            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int extracted = machine.extractEnergy(requested, true);

            if (extracted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.extractEnergy(extracted, false);
        }

        @Override
        public long getAmount() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getEnergy();
        }

        @Override
        public long getCapacity() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getMaxEnergy();
        }

        @Override
        protected @NonNull Integer createSnapshot() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getEnergy();
        }

        @Override
        protected void readSnapshot(@NonNull Integer snapshot) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine != null) {
                machine.setEnergy(snapshot);
            }
        }
    }

    public static void pushEnergy(Level level, BlockPos pos,
                                  EnergyLiquifierBlockEntity machine) {
        if (!machine.isReversed() || machine.getEnergy() <= 0) {
            return;
        }

        Direction front = machine.getBlockState()
                .getValue(EnergyLiquifierBlock.FACING);

        Direction outputSide = front.getOpposite();
        BlockPos targetPos = pos.relative(outputSide);

        if (!level.hasChunkAt(targetPos)) {
            return;
        }

        EnergyStorage source = EnergyStorage.SIDED.find(
                level, pos, outputSide);

        EnergyStorage target = EnergyStorage.SIDED.find(
                level, targetPos, front);

        if (source == null || target == null || !target.supportsInsertion()) {
            return;
        }

        long maxTransfer = Math.min(1_000L, machine.getEnergy());

        EnergyStorageUtil.move(source, target, maxTransfer, null);
    }
}
