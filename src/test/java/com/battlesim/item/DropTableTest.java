package com.battlesim.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.Enemies;
import com.battlesim.dungeon.Wave;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.Gear;
import com.battlesim.model.GearRarity;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class DropTableTest {

    @Test
    public void goldUsesRankBaseTimesDifficulty() {
        assertEquals(15, DropTable.goldFor(EnemyRank.NORMAL, 1.0));
        assertEquals(40, DropTable.goldFor(EnemyRank.ELITE, 1.0));
        assertEquals(120, DropTable.goldFor(EnemyRank.BOSS, 1.0));
        assertEquals(17, DropTable.goldFor(EnemyRank.NORMAL, 1.15));
        assertEquals(0, DropTable.goldFor(EnemyRank.BOSS, 0));
    }

    @Test
    public void bossesAlwaysDropAPiece() {
        Wave boss = new Wave(List.of(Enemies.dragon()));
        RandomProvider alwaysMin = new RandomProvider() {
            @Override
            public double nextDouble() {
                return 0.0;
            }

            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        DropTable.Loot loot = DropTable.roll(boss, 1.0, alwaysMin);
        assertEquals(120, loot.getGold());
        assertEquals(1, loot.getDrops().size());
        Gear drop = loot.getDrops().get(0);
        assertEquals(GearRarity.LEGENDARY, drop.getRarity());
        assertEquals(0, drop.getLevel());
    }

    @Test
    public void normalsCanMiss() {
        RandomProvider never = new RandomProvider() {
            @Override
            public double nextDouble() {
                return 0.99;
            }
        };
        assertTrue(DropTable.maybeDrop(EnemyRank.NORMAL, never) == null);
        assertTrue(DropTable.maybeDrop(EnemyRank.ELITE, never) == null);
        assertFalse(DropTable.maybeDrop(EnemyRank.BOSS, never) == null);
    }

    @Test
    public void chestAlwaysPaysGoldAndOnePiece() {
        RandomProvider midRoll = new RandomProvider() {
            @Override
            public double nextDouble() {
                return 0.50;
            }

            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        DropTable.Loot early = DropTable.chest(10, 1.0, midRoll);
        assertEquals(80, early.getGold());
        assertEquals(1, early.getDrops().size());
        assertEquals(GearRarity.RARE, early.getDrops().get(0).getRarity());

        DropTable.Loot late = DropTable.chest(50, 1.0, midRoll);
        assertEquals(GearRarity.EPIC, late.getDrops().get(0).getRarity());
    }
}
