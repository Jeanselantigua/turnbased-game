package com.battlesim.model;

/** Drop quality. Higher rarity starts with more substats already rolled. */
public enum GearRarity {
    COMMON("Common", 0),
    RARE("Rare", 1),
    EPIC("Epic", 2),
    LEGENDARY("Legendary", 3);

    private final String label;
    private final int startingSubstats;

    GearRarity(String label, int startingSubstats) {
        this.label = label;
        this.startingSubstats = startingSubstats;
    }

    public String getLabel() {
        return label;
    }

    /** How many substats exist at +0, before level-up rolls. Cap is {@link Gear#MAX_SUBSTATS}. */
    public int getStartingSubstats() {
        return startingSubstats;
    }
}
