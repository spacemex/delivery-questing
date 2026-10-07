package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.job.JobDefinition;
import com.github.spacemex.deliveryquesting.task.definition.OfferDefinition;
import com.github.spacemex.deliveryquesting.task.definition.TaskDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class GroupEmail {

    private static final Codec<Type> TYPE_CODEC = Codec.STRING.xmap(value ->
            Type.valueOf(value.toUpperCase(Locale.ROOT)), type -> type.name().toLowerCase(Locale.ROOT));

    public static final Codec<GroupEmail> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            UUIDUtil.CODEC.fieldOf("id")
                                    .forGetter(GroupEmail::id),
                            TYPE_CODEC.fieldOf("type")
                                    .forGetter(GroupEmail::type),
                            Identifier.CODEC.fieldOf("reference_id")
                                    .forGetter(GroupEmail::referenceId),
                            Codec.BOOL.optionalFieldOf("read", false)
                                    .forGetter(GroupEmail::read))
                    .apply(instance, GroupEmail::new));

    private final UUID id;

    private final Type type;

    private final Identifier referenceId;

    private boolean read;

    private GroupEmail(UUID id, Type type, Identifier referenceId, boolean read) {
        this.id = Objects.requireNonNull(id, "id");

        this.type = Objects.requireNonNull(type, "type");

        this.referenceId = Objects.requireNonNull(referenceId, "referenceId");

        this.read = read;
    }

    public static GroupEmail contract(TaskDefinition task) {
        return new GroupEmail(UUID.randomUUID(), Type.CONTRACT, task.id(), false);
    }

    public static GroupEmail offer(OfferDefinition offer) {
        return new GroupEmail(UUID.randomUUID(), Type.OFFER, offer.id(), false);
    }

    public UUID id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public Identifier referenceId() {
        return referenceId;
    }

    public boolean read() {
        return read;
    }

    public static GroupEmail job(JobDefinition job) {
        return new GroupEmail(UUID.randomUUID(), Type.JOB, job.id(), false);
    }

    boolean markRead() {
        if (read) {
            return false;
        }

        read = true;

        return true;
    }

    public enum Type {
        CONTRACT,
        OFFER,
        JOB
    }
}