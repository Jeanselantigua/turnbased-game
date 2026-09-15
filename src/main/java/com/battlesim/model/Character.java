package com.battlesim.model;

import java.util.List;

/**
 * A battle participant: a name, a set of stats, an elemental affinity
 * (used for type-effectiveness on the moves they use/receive), a status
 * condition, and the moves they know.
 */
public class Character {

    private final String name;
    private final Stats stats;
    private final Type affinity; // this character's own elemental type, if you want STAB-style bonuses
    private final List<Move> moves;
    private Status status;

    public Character(String name, Stats stats, Type affinity, List<Move> moves) {
        this.name = name;
        this.stats = stats;
        this.affinity = affinity;
        this.moves = moves;
        this.status = Status.NONE;
    }
    
    public Move getMoveByName(String name) {
        for (Move move : moves) {
            if (move.getName().equalsIgnoreCase(name)) {
                return move;
            }
        }
        throw new IllegalArgumentException(this.getName() + " doesn't know " + name);
    }
    
    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public Type getAffinity() {
        return affinity;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isFainted() {
        return stats.isFainted();
    }
}
