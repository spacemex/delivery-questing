package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public class DeliveryQuestingFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DeliveryQuesting.initializeClientOnly();

        FluidRenderingRegistry.register(ModFluids.LIQUID_ENERGY.get(), ModFluids.LIQUID_ENERGY_FLOWING.get(),
                new FluidModel.Unbaked(new Material(
                        Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "block/liquid_energy_still")),
                        new Material(
                                Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "block/liquid_energy_flowing")),
                        null, null));
    }
}
