package com.battlesim.model;

/**
 * Core numeric attributes for a Character.
 * Kept as a plain data holder — the engine classes decide how these
 * numbers get used (damage formulas, turn order, etc).
 */
public class Stats {

    private int maxHp;
    private int currentHp;
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

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    /** Applies damage, clamping at 0. Returns the amount actually taken. */
    public int applyDamage(int amount) {
        int before = currentHp;
        currentHp = Math.max(0, currentHp - amount);
        return before - currentHp;
    }

    /** Heals, clamping at maxHp. Returns the amount actually restored. */
    public int heal(int amount) {
        int before = currentHp;
        currentHp = Math.min(maxHp, currentHp + amount);
        return currentHp - before;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getMagicAttack() {
        return magicAttack;
    }

    public int getMagicDefense() {
        return magicDefense;
    }

    public int getSpeed() {
        return speed;
    }
}
