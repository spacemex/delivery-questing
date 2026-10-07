package com.github.spacemex.deliveryquesting.compat.energy.neoforge;

import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
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

public final class DronePadEnergyCompat {

    private static final Map<DronePadBlockEntity, DronePadEnergyHandler> HANDLERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private DronePadEnergyCompat() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(DronePadEnergyCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.DRONE_PAD.get(),
                (pad, direction) ->
                        HANDLERS.computeIfAbsent(pad, DronePadEnergyHandler::new));
    }

    private static final class DronePadEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {

        private final WeakReference<DronePadBlockEntity> padReference;

        private DronePadEnergyHandler(DronePadBlockEntity pad) {
            this.padReference = new WeakReference<>(pad);
        }

        @Override
        public long getAmountAsLong() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0L;
            }

            return pad.getEnergy();
        }

        @Override
        public long getCapacityAsLong() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0L;
            }

            return pad.getMaxEnergy();
        }

        @Override
        public int insert(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0;
            }

            int accepted = pad.receiveEnergy(amount, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            pad.receiveEnergy(accepted, false);

            return accepted;
        }

        @Override
        public int extract(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            return 0;
        }

        @Override
        protected Integer createSnapshot() {
            DronePadBlockEntity pad = padReference.get();

            if (pad == null) {
                return 0;
            }

            return pad.getEnergy();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            DronePadBlockEntity pad = padReference.get();

            if (pad != null) {
                pad.setEnergy(snapshot);
            }
        }
    }
}