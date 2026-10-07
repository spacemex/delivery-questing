package com.github.spacemex.deliveryquesting.task.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ItemReward(Identifier item, int count) {
    public static final Codec<ItemReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(ItemReward::item),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count")
                    .forGetter(ItemReward::count)).apply(instance, ItemReward::new));

    public ItemReward {
        Objects.requireNonNull(item, "item");

        if (count <= 0) {
            throw new IllegalArgumentException("Item reward count must be greater than 0");
        }
    }
}
