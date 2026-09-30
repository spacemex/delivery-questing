package com.github.spacemex.deliveryquesting.registry;

public final class ModRegistries {

    public static void initialize() {
        ModDataComponents.initialize();

        ModBlocks.initialize();
        ModBlockEntities.initialize();

        ModItems.initialize();
        ModMenus.initialize();
    }
}
