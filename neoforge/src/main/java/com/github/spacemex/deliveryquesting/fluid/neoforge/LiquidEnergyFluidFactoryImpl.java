package com.github.spacemex.deliveryquesting.fluid.neoforge;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import com.github.spacemex.deliveryquesting.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LiquidEnergyFluidFactoryImpl {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.FLUID_TYPES,
                    DeliveryQuesting.MOD_ID
            );

    public static final DeferredHolder<FluidType, FluidType> LIQUID_ENERGY_TYPE =
            FLUID_TYPES.register(
                    "liquid_energy",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .descriptionId(
                                            "fluid.delivery_questing.liquid_energy"
                                    )
                                    .lightLevel(5)
                                    .canSwim(false)
                                    .canDrown(false)
                                    .sound(
                                            SoundActions.BUCKET_FILL,
                                            SoundEvents.BUCKET_FILL
                                    )
                                    .sound(
                                            SoundActions.BUCKET_EMPTY,
                                            SoundEvents.BUCKET_EMPTY
                                    )
                    )
            );

    private static final BaseFlowingFluid.Properties PROPERTIES =
            new BaseFlowingFluid.Properties(
                    LIQUID_ENERGY_TYPE,
                    ModFluids.LIQUID_ENERGY,
                    ModFluids.LIQUID_ENERGY_FLOWING
            )
                    .block(
                            () -> (LiquidBlock) ModBlocks.LIQUID_ENERGY.get()
                    )
                    .bucket(
                            ModItems.LIQUID_ENERGY_BUCKET
                    )
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(5)
                    .explosionResistance(100F);

    private LiquidEnergyFluidFactoryImpl() {
    }

    public static Fluid createSource() {
        return new BaseFlowingFluid.Source(PROPERTIES);
    }

    public static Fluid createFlowing() {
        return new BaseFlowingFluid.Flowing(PROPERTIES);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}