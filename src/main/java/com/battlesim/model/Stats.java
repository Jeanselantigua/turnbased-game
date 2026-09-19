package com.battlesim.model;

public class Stats {

    private int maxHp;
    private int currentHp;
    private int shieldHp;
    private int attack;
    private int defense;
    private int magicAttack;
    private int magicDefense;
    private int speed;

    public Stats(int maxHp, int attack, int defense, int magicAttack, int magicDefense, int speed) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.magicAttack = magicAttack;
        this.magicDefense = magicDefense;
        this.speed = speed;
    }

    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getShieldHp() { return shieldHp; }
    public boolean hasShield() { return shieldHp > 0; }
    public boolean isFainted() { return currentHp <= 0; }

    public int applyDamage(int amount) {
        int before = currentHp;
        currentHp = Math.max(0, currentHp - amount);
        return before - currentHp;
    }

    public int heal(int amount) {
        int before = currentHp;
        currentHp = Math.min(maxHp, currentHp + amount);
        return currentHp - before;
    }

    public void grantShield(int amount) {
        if (amount <= 0) {
            return;
        }
        this.shieldHp = amount;
    }

    /** Consumes shield HP first. Returns how much of {@code amount} was absorbed. */
    public int absorbWithShield(int amount) {
        if (amount <= 0 || shieldHp <= 0) {
            return 0;
        }
        int absorbed = Math.min(shieldHp, amount);
        shieldHp -= absorbed;
        return absorbed;
    }

    /**
     * Raises max HP and keeps current HP at the same percent of max
     * (full stays full, half stays half, 0 stays fainted).
     */
    public void increaseMaxHpPreservingPercent(int amount) {
        if (amount <= 0 || maxHp <= 0) {
            return;
        }
        double ratio = (double) currentHp / maxHp;
        maxHp += amount;
        currentHp = (int) Math.round(ratio * maxHp);
        currentHp = Math.max(0, Math.min(maxHp, currentHp));
    }

    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getMagicAttack() { return magicAttack; }
    public int getMagicDefense() { return magicDefense; }
    public int getSpeed() { return speed; }

    // Permanent stat growth, used by passives (e.g. Caveman's Frenzy) and,
    // later, leveling. Speed is clamped to at least 1 so turn delay stays defined.
    public void increaseAttack(int amount) { this.attack += amount; }
    public void increaseDefense(int amount) { this.defense += amount; }
    public void increaseMagicAttack(int amount) { this.magicAttack += amount; }
    public void increaseMagicDefense(int amount) { this.magicDefense += amount; }
    public void increaseSpeed(int amount) { this.speed = Math.max(1, this.speed + amount); }
}