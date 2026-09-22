package com.battlesim.model;

/** One rolled line on a gear piece (main or substat). */
public final class GearAffix {

    private final StatKind kind;
    private int value;

    public GearAffix(StatKind kind, int value) {
        if (kind == null) {
            throw new IllegalArgumentException("Affix needs a stat");
        }
        this.kind = kind;
        this.value = Math.max(0, value);
    }

    public StatKind getKind() {
        return kind;
    }

    public int getValue() {
        return value;
    }

    public void add(int amount) {
        if (amount > 0) {
            value += amount;
        }
    }
}
