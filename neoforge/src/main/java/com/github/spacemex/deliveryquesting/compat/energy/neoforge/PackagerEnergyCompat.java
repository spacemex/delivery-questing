package com.github.spacemex.deliveryquesting.compat.energy.neoforge;

import com.github.spacemex.deliveryquesting.block.entity.PackagerBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class PackagerEnergyCompat {

    private static final Map<PackagerBlockEntity, PackagerEnergyHandler> HANDLERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PackagerEnergyCompat() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(PackagerEnergyCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PACKAGER.get(),
                (packager, direction) ->
                        HANDLERS.computeIfAbsent(packager, PackagerEnergyHandler::new));
    }

    private static final class PackagerEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {

        private final WeakReference<PackagerBlockEntity> reference;

        private PackagerEnergyHandler(PackagerBlockEntity packager) {
            reference = new WeakReference<>(packager);
        }

        @Override
        public long getAmountAsLong() {
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0L : packager.getEnergy();
        }

        @Override
        public long getCapacityAsLong() {
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0L : packager.getMaxEnergy();
        }

        @Override
        public int insert(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            PackagerBlockEntity packager = reference.get();

            if (packager == null) {
                return 0;
            }

            int accepted = packager.receiveEnergy(amount, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            packager.receiveEnergy(accepted, false);

            return accepted;
        }

        @Override
        public int extract(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            return 0;
        }

        @Override
        protected Integer createSnapshot() {
            PackagerBlockEntity packager = reference.get();

            return packager == null ? 0 : packager.getEnergy();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            PackagerBlockEntity packager = reference.get();

            if (packager != null) {
                packager.setEnergy(snapshot);
            }
        }
    }
}