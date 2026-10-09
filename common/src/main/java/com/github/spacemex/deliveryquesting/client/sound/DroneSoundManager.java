package com.github.spacemex.deliveryquesting.client.sound;

import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;

public final class DroneSoundManager {

    private static final Map<DroneEntity, DroneSoundInstance> ACTIVE_SOUNDS = new WeakHashMap<>();

    private static boolean initialized;

    private DroneSoundManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        ClientTickEvent.CLIENT_POST.register(DroneSoundManager::tick);
    }

    private static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            ACTIVE_SOUNDS.clear();

            return;
        }

        SoundManager soundManager = minecraft.getSoundManager();

        for (Entity entity : minecraft.level.entitiesForRendering()) {

            if (!(entity instanceof DroneEntity drone)) {
                continue;
            }

            if (drone.isIdle() || drone.getEnergy() <= 0) {
                continue;
            }

            DroneSoundInstance existing = ACTIVE_SOUNDS.get(drone);

            if (existing != null && soundManager.isActive(existing)) {
                continue;
            }

            DroneSoundInstance sound = new DroneSoundInstance(drone);

            ACTIVE_SOUNDS.put(drone, sound);

            soundManager.play(sound);
        }

        ACTIVE_SOUNDS.entrySet().removeIf(entry -> entry.getKey().isRemoved() ||
                (entry.getKey().isIdle() && !soundManager.isActive(entry.getValue())));
    }
}