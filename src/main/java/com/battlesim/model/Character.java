package com.battlesim.model;

import java.util.List;

public class Character {

    private final String name;
    private final Stats stats;
    private final Type affinity;
    private final List<Move> moves;
    private final List<Passive> passives;
    private Status status;

    public Character(String name, Stats stats, Type affinity, List<Move> moves, List<Passive> passives) {
        this.name = name;
        this.stats = stats;
        this.affinity = affinity;
        this.moves = moves;
        this.passives = passives;
        this.status = Status.NONE;
    }

    /** Convenience constructor for a Character with no passives. */
    public Character(String name, Stats stats, Type affinity, List<Move> moves) {
        this(name, stats, affinity, moves, List.of());
    }

    public String getName() { return name; }
    public Stats getStats() { return stats; }
    public Type getAffinity() { return affinity; }
    public List<Move> getMoves() { return moves; }
    public List<Passive> getPassives() { return passives; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isFainted() { return stats.isFainted(); }

    public Move getMoveByName(String name) {
        for (Move move : moves) {
            if (move.getName().equalsIgnoreCase(name)) {
                return move;
            }
        }
        throw new IllegalArgumentException(this.name + " doesn't know " + name);
    }
}