package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.task.ItemReward;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record MailboxParcel(UUID id, String sender, List<ItemReward> items) {
    public static final Codec<MailboxParcel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(MailboxParcel::id),
            Codec.STRING.fieldOf("sender").forGetter(MailboxParcel::sender),
            ItemReward.CODEC.listOf().fieldOf("items").forGetter(MailboxParcel::items)).apply(instance, MailboxParcel::new));

    public MailboxParcel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(items, "items");

        if (sender.isBlank()) {
            throw new IllegalArgumentException("Mailbox parcel sender cannot be blank");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Mailbox parcel cannot be empty");
        }

        items = List.copyOf(items);
    }

    public static MailboxParcel create(String sender, List<ItemReward> items) {
        return new MailboxParcel(UUID.randomUUID(), sender, items);
    }

    public long itemCount() {
        long count = 0L;

        for (ItemReward item : items) {
            count = Math.addExact(count, item.count());
        }
        return count;
    }
}