
package com.github.spacemex.deliveryquesting.compat.fluid.fabric;

import com.github.spacemex.deliveryquesting.block.EnergyLiquifierBlock;
import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class EnergyLiquifierFluidCompat {

    private static final long DROPLETS_PER_MB =
            FluidConstants.BUCKET / 1_000L;

    private static final Map<EnergyLiquifierBlockEntity, Tank> TANKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EnergyLiquifierFluidCompat() {
    }

    public static void register() {
        FluidStorage.SIDED.registerForBlockEntity(
                (machine, side) -> {
                    Direction front = machine.getBlockState()
                            .getValue(EnergyLiquifierBlock.FACING);

                    if (side != null && side != front) {
                        return null;
                    }

                    return TANKS.computeIfAbsent(machine, Tank::new);
                },
                ModBlockEntities.ENERGY_LIQUIFIER.get()
        );
    }

    private static final class Tank
            extends SnapshotParticipant<Integer>
            implements SingleSlotStorage<FluidVariant> {

        private final WeakReference<EnergyLiquifierBlockEntity> reference;

        private Tank(EnergyLiquifierBlockEntity machine) {
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
        public @NonNull FluidVariant getResource() {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null || machine.getFluidAmount() <= 0) {
                return FluidVariant.blank();
            }

            return FluidVariant.of(ModFluids.LIQUID_ENERGY.get());
        }

        @Override
        public long getAmount() {
            EnergyLiquifierBlockEntity machine = reference.get();

            return machine == null
                    ? 0
                    : machine.getFluidAmount() * DROPLETS_PER_MB;
        }

        @Override
        public long getCapacity() {
            EnergyLiquifierBlockEntity machine = reference.get();

            return machine == null
                    ? 0
                    : machine.getFluidCapacity() * DROPLETS_PER_MB;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public long insert(@NonNull FluidVariant resource,
                           long maxAmount,
                           @NonNull TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            if (resource.isBlank()) {
                throw new IllegalArgumentException("Cannot insert an empty fluid");
            }

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null || maxAmount < DROPLETS_PER_MB
                    || !resource.equals(FluidVariant.of(ModFluids.LIQUID_ENERGY.get()))) {
                return 0;
            }

            int requested = (int) Math.min(
                    maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE);

            int accepted = machine.fillFluid(requested, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            return machine.fillFluid(accepted, false) * DROPLETS_PER_MB;
        }

        @Override
        public long extract(@NonNull FluidVariant resource,
                            long maxAmount,
                            @NonNull TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            if (resource.isBlank()) {
                throw new IllegalArgumentException("Cannot extract an empty fluid");
            }

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null || maxAmount < DROPLETS_PER_MB
                    || !resource.equals(getResource())) {
                return 0;
            }

            int requested = (int) Math.min(
                    maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE);

            int available = machine.drainFluid(requested, true);

            if (available <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            return machine.drainFluid(available, false) * DROPLETS_PER_MB;
        }

        @Override
        protected @NonNull Integer createSnapshot() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getFluidAmount();
        }

        @Override
        protected void readSnapshot(@NonNull Integer snapshot) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine != null) {
                machine.setFluidAmount(snapshot);
            }
        }
    }
}
