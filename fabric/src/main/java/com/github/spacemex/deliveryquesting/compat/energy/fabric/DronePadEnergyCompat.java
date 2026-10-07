package com.github.spacemex.deliveryquesting.compat.energy.fabric;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jspecify.annotations.NonNull;
import team.reborn.energy.api.EnergyStorage;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class DronePadEnergyCompat {

    private static final Map<DronePadBlockEntity, DronePadEnergyStorage> STORAGES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private DronePadEnergyCompat() {
    }

    public static void register() {
        EnergyStorage.SIDED.registerForBlockEntity((pad, direction) ->
                STORAGES.computeIfAbsent(pad, DronePadEnergyStorage::new), ModBlockEntities.DRONE_PAD.get());
    }

    private static final class DronePadEnergyStorage extends SnapshotParticipant<Integer> implements EnergyStorage {

        private final WeakReference<DronePadBlockEntity> padReference;

        private DronePadEnergyStorage(DronePadBlockEntity pad) {
            this.padReference = new WeakReference<>(pad);
        }

        @Override
        public boolean supportsInsertion() {
            return padReference.get() != null;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0L;
            }

            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);

            int accepted = pad.receiveEnergy(requested, true);

            if (accepted <= 0) {
                return 0L;
            }

            updateSnapshots(transaction);

            pad.receiveEnergy(accepted, false);

            return accepted;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            return 0L;
        }

        @Override
        public long getAmount() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0L;
            }

            return pad.getEnergy();
        }

        @Override
        public long getCapacity() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0L;
            }

            return pad.getMaxEnergy();
        }

        @Override
        protected @NonNull Integer createSnapshot() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0;
            }

            return pad.getEnergy();
        }

        @Override
        protected void readSnapshot(@NonNull Integer snapshot) {
            DronePadBlockEntity pad = padReference.get();

            if (pad != null) {
                pad.setEnergy(snapshot);
            }
        }
    }
}