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
    private int critRate;
    private int critDamage;

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
     * Raises max HP. Living characters also gain the same amount of current HP
     * (a small heal). Fainted characters stay at 0 — level-ups do not revive.
     */
    public void increaseMaxHp(int amount) {
        if (amount <= 0) {
            return;
        }
        this.maxHp += amount;
        if (currentHp > 0) {
            currentHp = Math.min(maxHp, currentHp + amount);
        }
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
    public int getCritRate() { return critRate; }
    public int getCritDamage() { return critDamage; }

    public int get(StatKind kind) {
        if (kind == null) {
            return 0;
        }
        switch (kind) {
            case HP:
                return maxHp;
            case ATTACK:
                return attack;
            case DEFENSE:
                return defense;
            case MAGIC_ATTACK:
                return magicAttack;
            case MAGIC_DEFENSE:
                return magicDefense;
            case SPEED:
                return speed;
            case CRIT_RATE:
                return critRate;
            case CRIT_DAMAGE:
                return critDamage;
            default:
                return 0;
        }
    }

    /**
     * Adds (or subtracts) a permanent stat. Unequipping gear uses a negative
     * amount. Speed stays at least 1; max HP stays at least 1 and current HP
     * is clamped down if it would exceed the new max.
     */
    public void add(StatKind kind, int amount) {
        if (kind == null || amount == 0) {
            return;
        }
        switch (kind) {
            case HP:
                if (amount > 0) {
                    increaseMaxHp(amount);
                } else {
                    maxHp = Math.max(1, maxHp + amount);
                    currentHp = Math.min(currentHp, maxHp);
                }
                break;
            case ATTACK:
                attack = Math.max(0, attack + amount);
                break;
            case DEFENSE:
                defense = Math.max(0, defense + amount);
                break;
            case MAGIC_ATTACK:
                magicAttack = Math.max(0, magicAttack + amount);
                break;
            case MAGIC_DEFENSE:
                magicDefense = Math.max(0, magicDefense + amount);
                break;
            case SPEED:
                speed = Math.max(1, speed + amount);
                break;
            case CRIT_RATE:
                critRate = Math.max(0, critRate + amount);
                break;
            case CRIT_DAMAGE:
                critDamage = Math.max(0, critDamage + amount);
                break;
            default:
                break;
        }
    }

    /** HP + ATK + DEF + MATK + MDEF + SPD. */
    public int baseStatTotal() {
        return maxHp + attack + defense + magicAttack + magicDefense + speed;
    }

    // Permanent stat growth, used by passives (e.g. Caveman's Frenzy) and,
    // later, leveling. Speed is clamped to at least 1 so turn delay stays defined.
    public void increaseAttack(int amount) { this.attack += amount; }
    public void increaseDefense(int amount) { this.defense += amount; }
    public void increaseMagicAttack(int amount) { this.magicAttack += amount; }
    public void increaseMagicDefense(int amount) { this.magicDefense += amount; }
    public void increaseSpeed(int amount) { this.speed = Math.max(1, this.speed + amount); }

    /**
     * Dungeon scaling: multiply combat stats. Keep current HP at full after scale
     * (enemies spawn fresh each wave). Speed must stay at least 1.
     */
    public void scaleAll(double multiplier) {
        if (multiplier == 1.0) {
            return;
        }
        this.maxHp = Math.max(1, (int) Math.round(maxHp * multiplier));
        this.currentHp = maxHp;
        this.attack = Math.max(0, (int) Math.round(attack * multiplier));
        this.defense = Math.max(0, (int) Math.round(defense * multiplier));
        this.magicAttack = Math.max(0, (int) Math.round(magicAttack * multiplier));
        this.magicDefense = Math.max(0, (int) Math.round(magicDefense * multiplier));
        this.speed = Math.max(1, (int) Math.round(speed * multiplier));
    }
}