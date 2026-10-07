package com.github.spacemex.deliveryquesting.progression.entry;

import com.github.spacemex.deliveryquesting.task.entry.ItemReward;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record MailboxParcel(UUID id, String sender, List<ItemReward> items, Optional<Identifier> contractTaskId) {

    public static final Codec<MailboxParcel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            UUIDUtil.CODEC.fieldOf("id")
                                    .forGetter(MailboxParcel::id),
                            Codec.STRING.fieldOf("sender")
                                    .forGetter(MailboxParcel::sender),
                            ItemReward.CODEC.listOf().fieldOf("items")
                                    .forGetter(MailboxParcel::items),
                            Identifier.CODEC.optionalFieldOf("contract_task")
                                    .forGetter(MailboxParcel::contractTaskId))
                    .apply(instance, MailboxParcel::new));

    public MailboxParcel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(contractTaskId, "contractTaskId");

        if (sender.isBlank()) {
            throw new IllegalArgumentException("Mailbox parcel sender cannot be blank");
        }

        if (items.isEmpty() && contractTaskId.isEmpty()) {
            throw new IllegalArgumentException("Mailbox parcel cannot be empty");
        }

        items = List.copyOf(items);
    }

    public static MailboxParcel create(String sender, List<ItemReward> items) {
        return new MailboxParcel(UUID.randomUUID(), sender, items, Optional.empty());
    }

    public static MailboxParcel createContract(String sender, Identifier taskId) {
        return new MailboxParcel(UUID.randomUUID(), sender, List.of(), Optional.of(taskId));
    }

    public boolean isContractEnvelope() {
        return contractTaskId.isPresent();
    }

    public long itemCount() {
        long count = contractTaskId.isPresent() ? 1L : 0L;

        for (ItemReward item : items) {
            count = Math.addExact(count, item.count());
        }

        return count;
    }
}