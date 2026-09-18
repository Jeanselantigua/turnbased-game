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
    CURSED,     // e.g. 15% Current MagicAttack for 3 turns
    AFTERMATH,  // e.g. 15% max health dmg on death
    PARALYSIS,  // e.g. chance to skip a turn
    STUN,        // e.g. guaranteed skip next turn
    HEAL,       // e.g. heal for a percentage of the target's max health
    LEECH,      // e.g. 15% of damage dealt is converted to health
    SIPHON,     // DoT on the target; a portion heals the caster each tick
    ;

    /**
     * Number of end-of-turn ticks before the status clears.
     * 0 means it lasts until replaced or cleared by other logic (e.g. STUN).
     */
    public int getDefaultDurationTurns() {
        switch (this) {
            case CURSED:
                return 3;
            case BURN:
                return 3;
            case POISON:
                return 3;
            case BLEED:
                return 3;
            case PARALYSIS:
                return 3;
            case SIPHON:
                return 3;
            default:
                return 0;
        }
    }

    /** Only HEAL is used on living allies (including self). */
    public boolean targetsAllies() {
        return this == HEAL;
    }
}
