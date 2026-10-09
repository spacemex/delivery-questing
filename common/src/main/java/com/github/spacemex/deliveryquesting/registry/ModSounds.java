package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    private ModSounds() {}

    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> DRONE;
    public static final RegistrySupplier<SoundEvent> DRONE_CRASH;


    private static RegistrySupplier<SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, name);

        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        SOUNDS.register();
    }

    static {
        DRONE = register("drone");
        DRONE_CRASH = register("drone_crash");
    }
}