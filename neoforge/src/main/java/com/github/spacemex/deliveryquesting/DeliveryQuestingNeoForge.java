package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.compat.energy.neoforge.DronePadEnergyCompat;
import com.github.spacemex.deliveryquesting.compat.energy.neoforge.EnergyLiquifierCompat;
import com.github.spacemex.deliveryquesting.compat.energy.neoforge.PackagerEnergyCompat;
import com.github.spacemex.deliveryquesting.compat.fluid.neoforge.BarrelFluidCompat;
import com.github.spacemex.deliveryquesting.fluid.neoforge.LiquidEnergyFluidFactoryImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DeliveryQuesting.MOD_ID)
public final class DeliveryQuestingNeoForge {

    public DeliveryQuestingNeoForge(IEventBus eventBus) {
        LiquidEnergyFluidFactoryImpl.register(eventBus);

        DronePadEnergyCompat.register(eventBus);
        PackagerEnergyCompat.register(eventBus);
        EnergyLiquifierCompat.register(eventBus);
        BarrelFluidCompat.register(eventBus);

        DeliveryQuesting.initialize();
    }
}
