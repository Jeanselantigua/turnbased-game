package com.battlesim.model;

/**
 * Status conditions a Character can be afflicted with.
 * The engine (not this enum) decides what each one actually does each turn.
 */
public enum Status {
    NONE,
    BURN,       // e.g. damage over time
    POISON,     // e.g. damage over time
    BLEED,      // DoT: 40% of the inflictor's attack, snapshotted as status magnitude
    WOUNDED,    // Rogue stacks; detonated by Assassinate, not a ticking DoT
    CURSED,     // e.g. 15% Current MagicAttack for 3 turns
    AFTERMATH,  // e.g. 15% max health dmg on death
    PARALYSIS,  // e.g. chance to skip a turn
    STUN,        // e.g. guaranteed skip next turn
    SLOW,       // e.g. halved speed for 3 turns
    HEAL,       // e.g. heal for a percentage of the target's max health
    SHIELD,     // grant a shield equal to the move's power to an ally (including self)
    SELF_SHIELD, // grant a shield equal to the move's power to the caster only
    LEECH,      // e.g. 15% of damage dealt is converted to health
    SIPHON,     // DoT on the target; a portion heals the caster each tick
    UTILITY,    // Self/ally no-op (e.g. Recover). Not a combat status.
    ;

    /**
     * Number of start-of-turn ticks before the status clears.
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
            case WOUNDED:
                return 2;
            case PARALYSIS:
                return 3;
            case SLOW:
                return 3;
            case SIPHON:
                return 3;
            default:
                return 0;
        }
    }

    /** Friendly moves: heals, shields, and utility. Includes self-only shields. */
    public boolean targetsAllies() {
        return this == HEAL || this == SHIELD || this == SELF_SHIELD || this == UTILITY;
    }

    /** True for moves that may only target the caster. */
    public boolean targetsSelfOnly() {
        return this == SELF_SHIELD;
    }

    /** Harmful combat statuses used to classify "debuff-category" moves. */
    public boolean isDebuff() {
        switch (this) {
            case BURN:
            case POISON:
            case BLEED:
            case WOUNDED:
            case CURSED:
            case PARALYSIS:
            case STUN:
            case SLOW:
            case SIPHON:
                return true;
            default:
                return false;
        }
    }

    /** Reapply raises power by 15% and does not extend duration. */
    public boolean stacksOnReapply() {
        return this == BURN || this == POISON || this == BLEED || this == SLOW;
    }

    /** Reapply restores a fresh copy (original duration / magnitude). */
    public boolean refreshesOnReapply() {
        return this == STUN || this == PARALYSIS || this == CURSED
                || this == SHIELD || this == SELF_SHIELD || this == AFTERMATH;
    }
}
