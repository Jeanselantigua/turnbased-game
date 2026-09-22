package com.battlesim.model;

/**
 * Named set on a piece. 2-piece / 4-piece effects refresh when the loadout
 * changes; see {@code com.battlesim.item.SetBonuses}.
 */
public enum GearSet {
    WARLORD("Warlord"),
    SAGE("Sage"),
    SWIFT("Swift"),
    BULWARK("Bulwark"),
    VAMPIRE("Vampire");

    public static final int TWO_PIECE = 2;
    public static final int FOUR_PIECE = 4;

    private final String label;

    GearSet(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
