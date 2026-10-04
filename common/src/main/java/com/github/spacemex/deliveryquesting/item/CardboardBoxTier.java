package com.github.spacemex.deliveryquesting.item;

import com.mojang.serialization.Codec;

public enum CardboardBoxTier {

    TIER_1(1, 1, 1),
    TIER_2(2, 4, 4),
    TIER_3(3, 9, 9),
    TIER_4(4, 18, 9),
    TIER_5(5, 27, 9),
    TIER_6(6, 54, 9);

    public static final Codec<CardboardBoxTier> CODEC = Codec.INT.xmap(CardboardBoxTier::fromLevel, CardboardBoxTier::level);

    private final int level;
    private final int slots;
    private final int columns;

    CardboardBoxTier(int level, int slots, int columns) {
        this.level = level;
        this.slots = slots;
        this.columns = columns;
    }

    public int level() {
        return level;
    }

    public int slots() {
        return slots;
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return (slots + columns - 1) / columns;
    }

    public static CardboardBoxTier fromLevel(int level) {
        for (CardboardBoxTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Unknown cardboard box tier: " + level);
    }
}