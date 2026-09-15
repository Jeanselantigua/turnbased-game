package com.battlesim.model;

/**
 * Status conditions a Character can be afflicted with.
 * The engine (not this enum) decides what each one actually does each turn.
 */
public enum Status {
    NONE,
    BURN,       // e.g. damage over time
    POISON,     // e.g. damage over time
    BLEED,      // e.g. damage over time
    PARALYSIS,  // e.g. chance to skip a turn
    STUN        // e.g. guaranteed skip next turn
}
