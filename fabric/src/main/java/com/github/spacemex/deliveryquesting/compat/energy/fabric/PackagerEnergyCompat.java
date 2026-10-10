package com.github.spacemex.deliveryquesting.compat.energy.fabric;

import com.github.spacemex.deliveryquesting.block.entity.PackagerBlockEntity;
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

public final class PackagerEnergyCompat {

    private static final Map<PackagerBlockEntity, PackagerEnergyStorage> STORAGES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PackagerEnergyCompat() {
    }

    public static void register() {
        EnergyStorage.SIDED
                .registerForBlockEntity((packager, direction) ->
                        STORAGES.computeIfAbsent(packager, PackagerEnergyStorage::new), ModBlockEntities.PACKAGER.get());
    }

    private static final class PackagerEnergyStorage extends SnapshotParticipant<Integer> implements EnergyStorage {

        private final WeakReference<PackagerBlockEntity> reference;

        private PackagerEnergyStorage(PackagerBlockEntity packager) {
            reference = new WeakReference<>(packager);
        }

        @Override
        public boolean supportsInsertion() {
            return reference.get() != null;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            PackagerBlockEntity packager = reference.get();

            if (packager == null) {
                return 0L;
            }

            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);

            int accepted = packager.receiveEnergy(requested, true);

            if (accepted <= 0) {
                return 0L;
            }

            updateSnapshots(transaction);

            packager.receiveEnergy(accepted, false);

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
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0L : packager.getEnergy();
        }

        @Override
        public long getCapacity() {
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0L : packager.getMaxEnergy();
        }

        @Override
        protected @NonNull Integer createSnapshot() {
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0 : packager.getEnergy();
        }

        @Override
        protected void readSnapshot(@NonNull Integer snapshot) {
            PackagerBlockEntity packager = reference.get();

            if (packager != null) {
                packager.setEnergy(snapshot);
            }
        }
    }
}