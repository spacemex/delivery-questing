package com.example.mod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {

    public ExampleModNeoForge(IEventBus eventBus) {
        ExampleMod.initialize();
    }
}
