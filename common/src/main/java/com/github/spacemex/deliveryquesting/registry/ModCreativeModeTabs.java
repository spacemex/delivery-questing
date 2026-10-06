package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeModeTabs {
    public static DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static RegistrySupplier<CreativeModeTab> DELIVERY_TAB;

    public static void initialize(){
        TABS.register();
    }

    static {
        DELIVERY_TAB = TABS.register("delivery_tab", () -> CreativeTabRegistry.create(Component.literal("Delivery"),
                ()-> new ItemStack(ModItems.CARDBOARD_BOX_TIER_6.get())));
    }
}
