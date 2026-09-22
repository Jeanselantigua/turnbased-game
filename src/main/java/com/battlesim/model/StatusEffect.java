package com.battlesim.model;

/** One running instance of a status on a character. */
public final class StatusEffect {

    public static final double STACK_BONUS = 0.15;

    private final Status type;
    private int magnitude;
    private int turnsRemaining;
    private int stacks = 1;
    private double effectiveness = 1.0;

    public StatusEffect(Status type, int magnitude, int turnsRemaining) {
        this.type = type;
        this.magnitude = Math.max(0, magnitude);
        this.turnsRemaining = Math.max(0, turnsRemaining);
    }

    public Status getType() {
        return type;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public int getStacks() {
        return stacks;
    }

    public double getEffectiveness() {
        return effectiveness;
    }

    /** Extra applications raise power; duration is not extended. */
    public void stackEffectiveness() {
        stacks++;
        effectiveness = 1.0 + STACK_BONUS * (stacks - 1);
    }

    /** Re-apply as a fresh copy of this status (full duration, this magnitude). */
    public void resetToFresh(int magnitude, int durationTurns) {
        this.magnitude = Math.max(0, magnitude);
        this.turnsRemaining = Math.max(0, durationTurns);
        this.stacks = 1;
        this.effectiveness = 1.0;
    }

    /** @return true if the status expired */
    public boolean decrementTurn() {
        if (turnsRemaining <= 0) {
            return false;
        }
        turnsRemaining--;
        return turnsRemaining == 0;
    }
}
