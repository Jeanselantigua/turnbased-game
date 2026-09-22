package com.battlesim.progress;

import com.battlesim.model.StatKind;

/**
 * How much a level-up is worth. Everyone gets a small HP and speed bump
 * so glass cannons still grow, but skipping those points keeps you
 * fragile and slow.
 */
public final class Growth {

    public static final int AUTO_HP_PER_LEVEL = 4;
    public static final int AUTO_SPEED_PER_LEVEL = 1;
    public static final int ULT_UNLOCK_LEVEL = 18;
    public static final int ULT_COOLDOWN_TURNS = 3;
    public static final int POWER_PER_MOVE_POINT = 5;
    public static final double SCALING_PER_MOVE_POINT = 0.05;

    public static final int HP_PER_POINT = 3;
    public static final int HP_PER_SPECIALTY_POINT = 4;
    public static final int COMBAT_PER_POINT = 3;
    public static final int COMBAT_PER_SPECIALTY_POINT = 4;
    public static final int SPEED_PER_POINT = 2;
    public static final int SPEED_PER_SPECIALTY_POINT = 3;

    private Growth() {
    }

    public static int pointGain(StatKind kind, boolean specialty) {
        if (kind == StatKind.HP) {
            return specialty ? HP_PER_SPECIALTY_POINT : HP_PER_POINT;
        }
        if (kind == StatKind.SPEED) {
            return specialty ? SPEED_PER_SPECIALTY_POINT : SPEED_PER_POINT;
        }
        if (!kind.canSpendPoints()) {
            return 0;
        }
        return specialty ? COMBAT_PER_SPECIALTY_POINT : COMBAT_PER_POINT;
    }
}
