package com.github.spacemex.deliveryquesting.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;

public record PhysicalContractReservation(UUID groupId, Identifier taskId) {
    public static final Codec<PhysicalContractReservation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("group")
                            .forGetter(PhysicalContractReservation::groupId),
                    Identifier.CODEC.fieldOf("task")
                            .forGetter(PhysicalContractReservation::taskId))
            .apply(instance, PhysicalContractReservation::new));

    public PhysicalContractReservation {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(taskId, "taskId");
    }
}