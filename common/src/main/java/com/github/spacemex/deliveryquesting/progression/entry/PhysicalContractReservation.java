package com.github.spacemex.deliveryquesting.progression.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;

public record PhysicalContractReservation(UUID groupId, Identifier taskId, long lastIssuedGameTime) {

    public static final Codec<PhysicalContractReservation> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(UUIDUtil.CODEC.fieldOf("group")
                                    .forGetter(PhysicalContractReservation::groupId),
                            Identifier.CODEC.fieldOf("task")
                                    .forGetter(PhysicalContractReservation::taskId),
                            Codec.LONG.optionalFieldOf("last_issued_game_time", -1L)
                                    .forGetter(PhysicalContractReservation::lastIssuedGameTime))
                    .apply(instance, PhysicalContractReservation::new));

    public PhysicalContractReservation {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(taskId, "taskId");
    }
}