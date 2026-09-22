package com.battlesim.model;

/**
 * Combat stats. {@link #pointStats()} are the six you spend level-up points on.
 * Crit rate / crit damage come from gear, sets, and passives only.
 */
public enum StatKind {
    HP("HP", true, false),
    ATTACK("Attack", true, false),
    DEFENSE("Defense", true, false),
    MAGIC_ATTACK("Magic Attack", true, false),
    MAGIC_DEFENSE("Magic Defense", true, false),
    SPEED("Speed", true, false),
    CRIT_RATE("Crit Rate", false, true),
    CRIT_DAMAGE("Crit DMG", false, true);

    private static final StatKind[] POINT_STATS = {
            HP, ATTACK, DEFENSE, MAGIC_ATTACK, MAGIC_DEFENSE, SPEED
    };

    private final String label;
    private final boolean allocatable;
    private final boolean percent;

    StatKind(String label, boolean allocatable, boolean percent) {
        this.label = label;
        this.allocatable = allocatable;
        this.percent = percent;
    }

    public String getLabel() {
        return label;
    }

    public boolean canSpendPoints() {
        return allocatable;
    }

    public boolean isPercent() {
        return percent;
    }

    public String formatBonus(int value) {
        String sign = value < 0 ? "" : "+";
        return getLabel() + " " + sign + value + (percent ? "%" : "");
    }

    /** The six stats that receive level-up points. */
    public static StatKind[] pointStats() {
        return POINT_STATS.clone();
    }
}
