package com.battlesim.progress;

import static org.junit.Assert.assertEquals;
import com.battlesim.content.Enemies;
import com.battlesim.dungeon.Wave;
import com.battlesim.model.EnemyRank;
import java.util.List;
import org.junit.Test;

public class ExperienceTest {

    @Test
    public void enemyXpUsesRankBaseTimesDifficulty() {
        assertEquals(100, Experience.forEnemy(EnemyRank.NORMAL, 1.0));
        assertEquals(200, Experience.forEnemy(EnemyRank.ELITE, 1.0));
        assertEquals(750, Experience.forEnemy(EnemyRank.BOSS, 1.0));
        assertEquals(115, Experience.forEnemy(EnemyRank.NORMAL, 1.15));
        assertEquals(1088, Experience.forEnemy(EnemyRank.BOSS, 1.45));
    }

    @Test
    public void waveXpSumsEveryEnemy() {
        Wave mixed = new Wave(List.of(Enemies.goblin(), Enemies.orc()));
        assertEquals(100 + 200, Experience.forWave(mixed, 1.0));
        assertEquals((int) Math.round(300 * 1.15), Experience.forWave(mixed, 1.15));
    }

    @Test
    public void zeroOrNegativeScaleGivesNoXp() {
        assertEquals(0, Experience.forEnemy(EnemyRank.NORMAL, 0));
        assertEquals(0, Experience.forEnemy(EnemyRank.BOSS, -1));
    }
}
