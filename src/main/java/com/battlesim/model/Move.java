package com.battlesim.model;

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

    public Move(String name, Type type, int power, int accuracy, int priority,
                boolean isMagic, Status inflictedStatus, int statusChance) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.priority = priority;
        this.isMagic = isMagic;
        this.inflictedStatus = inflictedStatus;
        this.statusChance = statusChance;
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

    public boolean targetsAllies() {
        return inflictedStatus.targetsAllies();
    }
}
