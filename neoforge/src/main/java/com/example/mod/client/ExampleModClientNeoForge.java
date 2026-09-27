package com.example.mod.client;

import com.example.mod.ExampleMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
public final class ExampleModClientNeoForge {

    public ExampleModClientNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::clientInitialization);
    }

    private void clientInitialization(FMLClientSetupEvent event) {
        ExampleMod.initializeClientOnly();
    }
}
