package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.client.render.DroneRenderer;
import com.github.spacemex.deliveryquesting.client.screen.*;
import com.github.spacemex.deliveryquesting.client.sound.DroneSoundManager;
import com.github.spacemex.deliveryquesting.networking.ClientConfigSyncHandler;
import com.github.spacemex.deliveryquesting.registry.ModEntities;
import com.github.spacemex.deliveryquesting.registry.ModFluids;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = DeliveryQuesting.MOD_ID, dist = Dist.CLIENT)
public final class DeliveryQuestingClientNeoForge {

    public DeliveryQuestingClientNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::registerScreens);
        eventBus.addListener(this::registerEntityRenderers);
        eventBus.addListener(this::registerFluidModels);

        ClientConfigSyncHandler.initialize();

        DroneSoundManager.initialize();
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BULLETIN_BOARD.get(), BulletinBoardScreen::new);
        event.register(ModMenus.MAILBOX.get(), MailboxScreen::new);
        event.register(ModMenus.COMPUTER.get(), ComputerScreen::new);
        event.register(ModMenus.DELIVERY_CONTAINER.get(), DeliveryContainerScreen::new);
        event.register(ModMenus.CONTRACT.get(), ContractScreen::new);
        event.register(ModMenus.CARDBOARD_BOX.get(), CardboardBoxScreen::new);
        event.register(ModMenus.DRONE_PAD.get(), DronePadScreen::new);
        event.register(ModMenus.PACKAGER.get(), PackagerScreen::new);
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRONE.get(), DroneRenderer::new);
    }

    private void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(
                new Material(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "block/liquid_energy_still")),
                new Material(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "block/liquid_energy_flowing")),
                null, null
        ), ModFluids.LIQUID_ENERGY, ModFluids.LIQUID_ENERGY_FLOWING);
    }
}