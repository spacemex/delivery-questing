package com.github.spacemex.deliveryquesting.item.tier;

public enum UpgradeTier {

    TIER_1(1),
    TIER_2(2),
    TIER_3(3),
    TIER_4(4),
    TIER_5(5),
    TIER_6(6);

    private final int level;

    UpgradeTier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static UpgradeTier fromLevel(int level) {
        for (UpgradeTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }

        throw new IllegalArgumentException("Unknown upgrade tier: " + level);
    }
}