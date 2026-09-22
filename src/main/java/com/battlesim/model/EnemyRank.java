package com.battlesim.model;

/** How dangerous an enemy is. Dungeon waves mix these; bosses get extra mechanics. */
public enum EnemyRank {
    NORMAL(100),
    ELITE(200),
    BOSS(750);

    private final int baseXp;

    EnemyRank(int baseXp) {
        this.baseXp = baseXp;
    }

    /** XP at difficulty 1.0; dungeon scale multiplies this. */
    public int getBaseXp() {
        return baseXp;
    }
}
