
package com.github.spacemex.deliveryquesting.compat.fluid.fabric;

import com.github.spacemex.deliveryquesting.block.entity.BarrelBlockEntity;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class BarrelFluidCompat {

    private static final long DROPLETS_PER_MB =
            FluidConstants.BUCKET / 1_000L;

    private static final Map<BarrelBlockEntity, BarrelTank> TANKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private BarrelFluidCompat() {
    }

    public static void register() {
        FluidStorage.SIDED.registerForBlockEntity(
                (barrel, direction) ->
                        TANKS.computeIfAbsent(barrel, BarrelTank::new),
                ModBlockEntities.BARREL.get()
        );
    }

    private static final class BarrelTank extends SnapshotParticipant<BarrelContents> implements SingleSlotStorage<FluidVariant> {

        private final WeakReference<BarrelBlockEntity> reference;

        private BarrelTank(BarrelBlockEntity barrel) {
            reference = new WeakReference<>(barrel);
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
            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || barrel.getContents().isEmpty()) {
                return FluidVariant.blank();
            }

            Fluid fluid = BuiltInRegistries.FLUID.getValue(barrel.getContents().fluid());

            if (fluid == null || fluid == Fluids.EMPTY) {
                return FluidVariant.blank();
            }

            return FluidVariant.of(fluid);
        }

        @Override
        public long getAmount() {
            BarrelBlockEntity barrel = reference.get();

            return barrel == null ? 0L : barrel.getContents().amount() * DROPLETS_PER_MB;
        }

        @Override
        public long getCapacity() {
            BarrelBlockEntity barrel = reference.get();

            return barrel == null ? 0L : barrel.getCapacity() * DROPLETS_PER_MB;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public long insert(@NonNull FluidVariant resource, long maxAmount, @NonNull TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            if (resource.isBlank()) {
                throw new IllegalArgumentException("Cannot insert an empty fluid");
            }

            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || maxAmount < DROPLETS_PER_MB) {
                return 0L;
            }

            if (resource.hasComponents()) {
                return 0L;
            }

            Identifier fluidId = BuiltInRegistries.FLUID.getKey(resource.getFluid());

            int requestedMB = (int) Math.min(maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE);

            int acceptedMB = barrel.fill(fluidId, requestedMB, true);

            if (acceptedMB <= 0) {
                return 0L;
            }

            updateSnapshots(transaction);

            int insertedMB = barrel.fill(fluidId, acceptedMB, false);

            return insertedMB * DROPLETS_PER_MB;
        }

        @Override
        public long extract(@NonNull FluidVariant resource, long maxAmount, @NonNull TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            if (resource.isBlank()) {
                throw new IllegalArgumentException("Cannot extract an empty fluid");
            }

            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || maxAmount < DROPLETS_PER_MB) {
                return 0L;
            }

            if (!resource.equals(getResource())) {
                return 0L;
            }

            int requestedMB = (int) Math.min(maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE);

            int availableMB = barrel.drain(requestedMB, true);

            if (availableMB <= 0) {
                return 0L;
            }

            updateSnapshots(transaction);

            int extractedMB = barrel.drain(availableMB, false);

            return extractedMB * DROPLETS_PER_MB;
        }

        @Override
        protected @NonNull BarrelContents createSnapshot() {
            BarrelBlockEntity barrel = reference.get();

            return barrel == null ? BarrelContents.EMPTY : barrel.getContents();
        }

        @Override
        protected void readSnapshot(@NonNull BarrelContents snapshot) {
            BarrelBlockEntity barrel = reference.get();

            if (barrel != null) {
                barrel.restoreContents(snapshot);
            }
        }
    }
}
