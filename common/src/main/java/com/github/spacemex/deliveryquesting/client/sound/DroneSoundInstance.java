package com.github.spacemex.deliveryquesting.client.sound;

import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.registry.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public final class DroneSoundInstance extends AbstractTickableSoundInstance {

    private final DroneEntity drone;

    public DroneSoundInstance(DroneEntity drone) {
        super(ModSounds.DRONE.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());

        this.drone = drone;

        volume = 1F;

        pitch = drone.getEnginePitch();

        looping = true;

        updatePosition();
    }

    @Override
    public void tick() {
        if (drone.isRemoved() || drone.getEnergy() <= 0 || drone.isIdle()) {
            stop();

            return;
        }

        pitch = drone.getEnginePitch();

        updatePosition();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    private void updatePosition() {
        x = drone.getX();

        y = drone.getY();

        z = drone.getZ();
    }
}