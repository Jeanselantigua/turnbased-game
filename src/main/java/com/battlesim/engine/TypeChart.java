package com.battlesim.engine;

import com.battlesim.model.Type;

/**
 * Looks up damage multipliers between an attacking Move's Type and a
 * defending Character's affinity Type.
 * TODO: fill in the multiplier table.
 */
public class TypeChart {

    public double getMultiplier(Type attackingType, Type defendingType) {
        // TODO: return 2.0 for super effective, 0.5 for resisted, 1.0 for neutral
        return 1.0;
    }
}
