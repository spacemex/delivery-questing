
package com.github.spacemex.deliveryquesting.item.tier;

import com.mojang.serialization.Codec;

public enum BarrelTier {
    TIER_1(1, 1_000),
    TIER_2(2, 4_000),
    TIER_3(3, 16_000),
    TIER_4(4, 64_000),
    TIER_5(5, 256_000),
    TIER_6(6, 1_024_000);

    public static final Codec<BarrelTier> CODEC = Codec.INT.xmap(BarrelTier::fromLevel, BarrelTier::level);

    private final int level;
    private final int capacity;

    BarrelTier(int level, int capacity) {
        this.level = level;
        this.capacity = capacity;
    }

    public int level() {
        return level;
    }

    public int capacity() {
        return capacity;
    }

    public static BarrelTier fromLevel(int level) {
        for (BarrelTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }

        throw new IllegalArgumentException("Unknown barrel tier: " + level);
    }
}
