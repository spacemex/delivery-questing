
package com.github.spacemex.deliveryquesting.compat;

import com.github.spacemex.deliveryquesting.block.EnergyLiquifierBlock;
import com.github.spacemex.deliveryquesting.block.entity.EnergyLiquifierBlockEntity;
import com.github.spacemex.deliveryquesting.registry.ModBlockEntities;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class EnergyLiquifierCompat {

    private static final Map<EnergyLiquifierBlockEntity, MachineEnergy> ENERGIES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<EnergyLiquifierBlockEntity, MachineFluid> FLUIDS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EnergyLiquifierCompat() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(EnergyLiquifierCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.ENERGY_LIQUIFIER.get(),
                (machine, side) -> {
                    Direction front = machine.getBlockState()
                            .getValue(EnergyLiquifierBlock.FACING);

                    if (side != null && side != front.getOpposite()) {
                        return null;
                    }

                    return ENERGIES.computeIfAbsent(machine, MachineEnergy::new);
                }
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.ENERGY_LIQUIFIER.get(),
                (machine, side) -> {
                    Direction front = machine.getBlockState()
                            .getValue(EnergyLiquifierBlock.FACING);

                    if (side != null && side != front) {
                        return null;
                    }

                    return FLUIDS.computeIfAbsent(machine, MachineFluid::new);
                }
        );
    }

    private static final class MachineEnergy
            extends SnapshotJournal<Integer>
            implements EnergyHandler {

        private final WeakReference<EnergyLiquifierBlockEntity> reference;

        private MachineEnergy(EnergyLiquifierBlockEntity machine) {
            reference = new WeakReference<>(machine);
        }

        @Override
        public long getAmountAsLong() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getEnergy();
        }

        @Override
        public long getCapacityAsLong() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getMaxEnergy();
        }

        @Override
        public int insert(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null) {
                return 0;
            }

            int accepted = machine.receiveEnergy(amount, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.receiveEnergy(accepted, false);
        }

        @Override
        public int extract(int amount, @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null) {
                return 0;
            }

            int extracted = machine.extractEnergy(amount, true);

            if (extracted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.extractEnergy(extracted, false);
        }

        @Override
        protected Integer createSnapshot() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getEnergy();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine != null) {
                machine.setEnergy(snapshot);
            }
        }
    }

    private static final class MachineFluid
            extends SnapshotJournal<Integer>
            implements ResourceHandler<FluidResource> {

        private final WeakReference<EnergyLiquifierBlockEntity> reference;

        private MachineFluid(EnergyLiquifierBlockEntity machine) {
            reference = new WeakReference<>(machine);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public @NonNull FluidResource getResource(int index) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (index != 0 || machine == null
                    || machine.getFluidAmount() <= 0) {
                return FluidResource.EMPTY;
            }

            return FluidResource.of(ModFluids.LIQUID_ENERGY.get());
        }

        @Override
        public long getAmountAsLong(int index) {
            EnergyLiquifierBlockEntity machine = reference.get();

            return index != 0 || machine == null
                    ? 0 : machine.getFluidAmount();
        }

        @Override
        public long getCapacityAsLong(int index,
                                      @NonNull FluidResource resource) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (index != 0 || machine == null) {
                return 0;
            }

            return resource.isEmpty() || isValid(index, resource)
                    ? machine.getFluidCapacity()
                    : 0;
        }

        @Override
        public boolean isValid(int index, @NonNull FluidResource resource) {
            return index == 0
                    && !resource.isEmpty()
                    && resource.isComponentsPatchEmpty()
                    && resource.getFluid() == ModFluids.LIQUID_ENERGY.get();
        }

        @Override
        public int insert(int index,
                          @NonNull FluidResource resource,
                          int amount,
                          @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null || !isValid(index, resource)) {
                return 0;
            }

            int accepted = machine.fillFluid(amount, true);

            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.fillFluid(accepted, false);
        }

        @Override
        public int extract(int index,
                           @NonNull FluidResource resource,
                           int amount,
                           @NonNull TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine == null || index != 0
                    || !resource.equals(getResource(index))) {
                return 0;
            }

            int available = machine.drainFluid(amount, true);

            if (available <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            return machine.drainFluid(available, false);
        }

        @Override
        protected Integer createSnapshot() {
            EnergyLiquifierBlockEntity machine = reference.get();
            return machine == null ? 0 : machine.getFluidAmount();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            EnergyLiquifierBlockEntity machine = reference.get();

            if (machine != null) {
                machine.setFluidAmount(snapshot);
            }
        }
    }
}
