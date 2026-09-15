package com.battlesim.model;

/**
 * Elemental/damage type for a Move. Used to look up effectiveness
 * multipliers in TypeChart. Trim or expand this list to fit your game.
 */
public enum Type {
    PHYSICAL,
    FIRE,
    ICE,
    LIGHTNING,
    EARTH,
    ARCANE,
    HOLY,
    SHADOW
}
