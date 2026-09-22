package com.battlesim.progress;

import com.battlesim.dungeon.Wave;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.EnemyTemplate;

/**
 * XP from a defeated enemy / wave. Rank sets the base; dungeon difficulty
 * (floor block + party-size scale) multiplies it.
 */
public final class Experience {

    private Experience() {
    }

    public static int forEnemy(EnemyRank rank, double difficultyScale) {
        if (rank == null || difficultyScale <= 0) {
            return 0;
        }
        return Math.max(0, (int) Math.round(rank.getBaseXp() * difficultyScale));
    }

    public static int forWave(Wave wave, double difficultyScale) {
        if (wave == null) {
            return 0;
        }
        int total = 0;
        for (EnemyTemplate enemy : wave.getEnemies()) {
            total += forEnemy(enemy.getRank(), difficultyScale);
        }
        return total;
    }
}
