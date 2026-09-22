package com.battlesim.model;

import java.util.List;

/**
 * A single action a Character can take on their turn.
 * Kept intentionally simple for Phase 1 — no branching effects yet,
 * just enough to run a damage-based turn.
 */
public class Move {

    private final String name;
    private final Type type;
    private final int power;
    private final int accuracy;
    private final int priority;
    private final boolean isMagic;
    private final Status inflictedStatus;  // Status.NONE if this move doesn't apply one
    private final int statusChance;        // 0-100, chance to apply inflictedStatus on hit
    private final int cooldownTurns;       // 0 = no cooldown; remaining tracked on Character
    private final int minHits;
    private final int maxHits;

    public Move(String name, Type type, int power, int accuracy, int priority,
                boolean isMagic, Status inflictedStatus, int statusChance) {
        this(name, type, power, accuracy, priority, isMagic, inflictedStatus, statusChance, 0);
    }

    public Move(String name, Type type, int power, int accuracy, int priority,
                boolean isMagic, Status inflictedStatus, int statusChance, int cooldownTurns) {
        this(name, type, power, accuracy, priority, isMagic, inflictedStatus, statusChance,
                cooldownTurns, 1, 1);
    }

    public Move(String name, Type type, int power, int accuracy, int priority,
                boolean isMagic, Status inflictedStatus, int statusChance, int cooldownTurns,
                int minHits, int maxHits) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.priority = priority;
        this.isMagic = isMagic;
        this.inflictedStatus = inflictedStatus;
        this.statusChance = statusChance;
        this.cooldownTurns = Math.max(0, cooldownTurns);
        this.minHits = Math.max(1, minHits);
        this.maxHits = Math.max(this.minHits, maxHits);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isMagic() {
        return isMagic;
    }
    
    public Status getInflictedStatus() { 
    	return inflictedStatus; 
    }
    
    public int getStatusChance() { 
    	return statusChance; 
    }

    public int getCooldownTurns() {
        return cooldownTurns;
    }

    public int getMinHits() {
        return minHits;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public boolean targetsAllies() {
        return inflictedStatus.targetsAllies();
    }

    public boolean targetsSelfOnly() {
        return inflictedStatus.targetsSelfOnly();
    }

    public List<Character> legalTargets(Character actor, List<Character> allies, List<Character> enemies) {
        if (targetsSelfOnly()) {
            return List.of(actor);
        }
        return targetsAllies() ? allies : enemies;
    }

    public boolean isDebuffCategory() {
        return inflictedStatus.isDebuff();
    }
}
