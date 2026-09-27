package com.example.mod.client;

import com.example.mod.ExampleMod;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ExampleMod.initializeClientOnly();
    }
}
