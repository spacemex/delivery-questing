
package com.github.spacemex.deliveryquesting.compat.fluid.neoforge;

import com.github.spacemex.deliveryquesting.block.entity.BarrelBlockEntity;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class BarrelFluidCompat {

    private static final Map<BarrelBlockEntity, BarrelFluidHandler> HANDLERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private BarrelFluidCompat() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(BarrelFluidCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.BARREL.get(),
                (barrel, direction) ->
                        HANDLERS.computeIfAbsent(barrel, BarrelFluidHandler::new));
    }

    private static final class BarrelFluidHandler extends SnapshotJournal<BarrelContents> implements ResourceHandler<FluidResource> {

        private final WeakReference<BarrelBlockEntity> reference;

        private BarrelFluidHandler(BarrelBlockEntity barrel) {
            reference = new WeakReference<>(barrel);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public @NonNull FluidResource getResource(int index) {
            if (index != 0) {
                return FluidResource.EMPTY;
            }

            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || barrel.getContents().isEmpty()) {
                return FluidResource.EMPTY;
            }

            Fluid fluid = BuiltInRegistries.FLUID.getValue(barrel.getContents().fluid());

            if (fluid == null || fluid == Fluids.EMPTY) {
                return FluidResource.EMPTY;
            }

            return FluidResource.of(fluid);
        }

        @Override
        public long getAmountAsLong(int index) {
            BarrelBlockEntity barrel = reference.get();

            if (index != 0 || barrel == null) {
                return 0L;
            }

            return barrel.getContents().amount();
        }

        @Override
        public long getCapacityAsLong(int index, @NonNull FluidResource resource) {
            BarrelBlockEntity barrel = reference.get();

            if (index != 0 || barrel == null) {
                return 0L;
            }

            if (!resource.isEmpty() && !isValid(index, resource)) {
                return 0L;
            }

            return barrel.getCapacity();
        }

        @Override
        public boolean isValid(int index, @NonNull FluidResource resource) {
            return index == 0 && !resource.isEmpty() && resource.isComponentsPatchEmpty() && resource.getFluid() != Fluids.EMPTY;
        }

        @Override
        public int insert(int index, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            if (!isValid(index, resource)) {
                return 0;
            }

            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || amount == 0) {
                return 0;
            }

            Identifier fluidId = BuiltInRegistries.FLUID.getKey(resource.getFluid());

            int accepted = barrel.fill(fluidId, amount, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            return barrel.fill(fluidId, accepted, false);
        }

        @Override
        public int extract(int index, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            if (index != 0 || amount == 0) {
                return 0;
            }

            BarrelBlockEntity barrel = reference.get();

            if (barrel == null || !resource.equals(getResource(0))) {
                return 0;
            }

            int available = barrel.drain(amount, true);

            if (available <= 0) {
                return 0;
            }

            updateSnapshots(transaction);

            return barrel.drain(available, false);
        }

        @Override
        protected @NonNull BarrelContents createSnapshot() {
            BarrelBlockEntity barrel = reference.get();

            return barrel == null ? BarrelContents.EMPTY : barrel.getContents();
        }

        @Override
        protected void revertToSnapshot(@NonNull BarrelContents snapshot) {
            BarrelBlockEntity barrel = reference.get();

            if (barrel != null) {
                barrel.restoreContents(snapshot);
            }
        }
    }
}
