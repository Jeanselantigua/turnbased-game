package com.battlesim.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character {

    private final String name;
    private final Stats stats;
    private final Type affinity;
    private final List<Move> moves;
    private final List<Passive> passives;
    private final Map<String, Integer> moveCooldowns = new HashMap<>();
    private Status status;
    private int statusTurnsRemaining;
    /** Snapshotted at apply time (e.g. inflictor attack for BLEED). 0 if unused. */
    private int statusMagnitude;
    private Character siphonSource;
    private int siphonTurnsRemaining;
    private Character summoner;
    private int blessedStacks;

    public Character(String name, Stats stats, Type affinity, List<Move> moves, List<Passive> passives) {
        this.name = name;
        this.stats = stats;
        this.affinity = affinity;
        this.moves = new ArrayList<>(moves);
        this.passives = new ArrayList<>(passives);
        this.status = Status.NONE;
        this.statusTurnsRemaining = 0;
        this.statusMagnitude = 0;
        this.siphonTurnsRemaining = 0;
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
    public int getStatusTurnsRemaining() { return statusTurnsRemaining; }
    public int getStatusMagnitude() { return statusMagnitude; }

    /** Speed used by turn order. SLOW halves it (minimum 1). */
    public int getEffectiveSpeed() {
        int speed = stats.getSpeed();
        if (status == Status.SLOW) {
            return Math.max(1, speed / 2);
        }
        return speed;
    }

    public void setStatus(Status status) {
        setStatus(status, 0);
    }

    public void setStatus(Status status, int magnitude) {
        this.status = status;
        this.statusTurnsRemaining = status.getDefaultDurationTurns();
        this.statusMagnitude = status == Status.NONE ? 0 : Math.max(0, magnitude);
    }

    /** Counts down timed statuses (e.g. CURSED). No-op when duration is infinite. */
    public void decrementStatusDuration() {
        if (statusTurnsRemaining <= 0) {
            return;
        }
        statusTurnsRemaining--;
        if (statusTurnsRemaining == 0) {
            this.status = Status.NONE;
            this.statusMagnitude = 0;
        }
    }

    public boolean isFainted() { return stats.isFainted(); }

    public Character getSiphonSource() { return siphonSource; }
    public int getSiphonTurnsRemaining() { return siphonTurnsRemaining; }
    public boolean isSiphoned() { return siphonTurnsRemaining > 0 && siphonSource != null; }

    public void applySiphon(Character source, int turns) {
        this.siphonSource = source;
        this.siphonTurnsRemaining = turns;
    }

    public void decrementSiphonDuration() {
        if (siphonTurnsRemaining <= 0) {
            return;
        }
        siphonTurnsRemaining--;
        if (siphonTurnsRemaining == 0) {
            siphonSource = null;
        }
    }

    public void clearSiphon() {
        siphonSource = null;
        siphonTurnsRemaining = 0;
    }

    public boolean isSummon() { return summoner != null; }
    public Character getSummoner() { return summoner; }
    public void setSummoner(Character summoner) { this.summoner = summoner; }

    public Move getMoveByName(String name) {
        for (Move move : moves) {
            if (move.getName().equalsIgnoreCase(name)) {
                return move;
            }
        }
        throw new IllegalArgumentException(this.name + " doesn't know " + name);
    }

    public void addMove(Move move) {
        for (Move existing : moves) {
            if (existing.getName().equals(move.getName())) {
                return;
            }
        }
        moves.add(move);
    }

    public void removeMoveByName(String name) {
        moves.removeIf(move -> move.getName().equals(name));
    }

    public void addPassive(Passive passive) {
        if (!passives.contains(passive)) {
            passives.add(passive);
        }
    }

    public void removePassivesOfType(Class<?> type) {
        passives.removeIf(type::isInstance);
    }

    public boolean hasPassive(Class<?> type) {
        for (Passive passive : passives) {
            if (type.isInstance(passive)) {
                return true;
            }
        }
        return false;
    }

    public <T extends Passive> T getPassive(Class<T> type) {
        for (Passive passive : passives) {
            if (type.isInstance(passive)) {
                return type.cast(passive);
            }
        }
        return null;
    }

    public int getBlessedStacks() {
        return blessedStacks;
    }

    public void addBlessedStacks(int amount, int cap) {
        if (amount <= 0 || cap <= 0) {
            return;
        }
        blessedStacks = Math.min(cap, blessedStacks + amount);
    }

    public void clearBlessedStacks() {
        blessedStacks = 0;
    }

    public void startMoveCooldown(String moveName, int turns) {
        if (turns <= 0) {
            return;
        }
        moveCooldowns.put(moveName, turns);
    }

    public int getMoveCooldown(String moveName) {
        return moveCooldowns.getOrDefault(moveName, 0);
    }

    public void tickMoveCooldowns() {
        moveCooldowns.replaceAll((name, remaining) -> Math.max(0, remaining - 1));
        moveCooldowns.values().removeIf(remaining -> remaining <= 0);
    }
}