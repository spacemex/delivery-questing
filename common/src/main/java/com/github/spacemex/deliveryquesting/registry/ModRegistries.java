package com.github.spacemex.deliveryquesting.registry;

public final class ModRegistries {

    private ModRegistries() {}

    public static void initialize() {
        ModDataComponents.initialize();

        ModSounds.initialize();

        ModBlocks.initialize();
        ModBlockEntities.initialize();

        ModEntities.initialize();

        ModItems.initialize();
        ModMenus.initialize();
        ModCreativeModeTabs.initialize();
    }
}
