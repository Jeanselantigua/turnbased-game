package com.battlesim.model;

import com.battlesim.progress.Growth;

/**
 * Level / XP / unspent points for a character. XP to the next level is
 * {@link #XP_PER_LEVEL_FACTOR} × current level (100, 200, 300, …).
 */
public class Progression {

    public static final int MAX_LEVEL = 30;
    public static final int XP_PER_LEVEL_FACTOR = 100;
    public static final int STAT_POINTS_PER_LEVEL = 1;
    public static final int STAT_POINTS_AT_CAP = (MAX_LEVEL - 1) * STAT_POINTS_PER_LEVEL;

    private int level = 1;
    private int xp = 0;
    private int unspentStatPoints = 0;
    private int unspentMovePoints = 0;

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getUnspentStatPoints() {
        return unspentStatPoints;
    }

    public int getUnspentMovePoints() {
        return unspentMovePoints;
    }

    /** XP needed to go from this level to the next. 0 at the cap. */
    public int xpToNextLevel() {
        if (level >= MAX_LEVEL) {
            return 0;
        }
        return XP_PER_LEVEL_FACTOR * level;
    }

    /** Total XP to climb from level 1 to {@code targetLevel} (capped at {@link #MAX_LEVEL}). */
    public static int xpToReachLevel(int targetLevel) {
        int last = Math.min(Math.max(targetLevel, 1), MAX_LEVEL) - 1;
        return XP_PER_LEVEL_FACTOR * last * (last + 1) / 2;
    }

    /**
     * Adds XP and levels until the remainder is not enough for another level.
     * @return how many levels were gained
     */
    public int addXp(int amount) {
        if (amount <= 0 || level >= MAX_LEVEL) {
            return 0;
        }
        xp += amount;
        int gained = 0;
        while (level < MAX_LEVEL && xp >= xpToNextLevel()) {
            xp -= xpToNextLevel();
            level++;
            gained++;
            unspentStatPoints += STAT_POINTS_PER_LEVEL;
            if (level > Growth.ULT_UNLOCK_LEVEL) {
                unspentMovePoints++;
            }
        }
        if (level >= MAX_LEVEL) {
            xp = 0;
        }
        return gained;
    }

    public boolean spendStatPoint() {
        if (unspentStatPoints <= 0) {
            return false;
        }
        unspentStatPoints--;
        return true;
    }

    public boolean spendMovePoint() {
        if (unspentMovePoints <= 0) {
            return false;
        }
        unspentMovePoints--;
        return true;
    }
}
