package com.battlesim.dungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.Enemies;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.item.DropTable;
import com.battlesim.model.Character;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.EnemyTemplate;
import com.battlesim.model.Inventory;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class DungeonTest {

    @Test
    public void scaleStaysFlatUntilTheNextBlockOfFifteen() {
        Dungeon dungeon = Dungeon.standard();
        assertEquals(Dungeon.STARTING_SCALE, dungeon.scaleForWave(1), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE, dungeon.scaleForWave(15), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(16), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(30), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + 2 * Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(31), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + 2 * Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(45), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + 3 * Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(46), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE + 6 * Dungeon.SCALE_PER_BLOCK, dungeon.scaleForWave(100), 0.0001);
    }

    @Test
    public void everyFifteenthWaveIsABoss() {
        Dungeon dungeon = Dungeon.standard(new RandomProvider(7L));
        assertEquals(Dungeon.DEFAULT_FLOORS, dungeon.getWaves().size());
        for (int waveNumber = 1; waveNumber <= dungeon.getWaves().size(); waveNumber++) {
            Wave wave = dungeon.getWaves().get(waveNumber - 1);
            boolean bossWave = Dungeon.isBossWave(waveNumber);
            boolean hasBoss = wave.getEnemies().stream().anyMatch(e -> e.getRank() == EnemyRank.BOSS);
            assertEquals("wave " + waveNumber, bossWave, hasBoss);
            if (bossWave) {
                assertEquals(1, wave.getEnemies().size());
            }
        }
    }

    @Test
    public void laterBlockSpawnsScaleEnemyStats() {
        Character base = Enemies.goblin().createInstance(1.0);
        Dungeon dungeon = Dungeon.standard();
        Character scaled = Enemies.goblin().createInstance(dungeon.scaleForWave(16));
        int goblinHp = base.getStats().getMaxHp();
        int expectedHp = (int) Math.round(goblinHp * dungeon.scaleForWave(16));
        assertEquals(60, goblinHp);
        assertEquals(expectedHp, scaled.getStats().getMaxHp());
        assertEquals(scaled.getStats().getMaxHp(), scaled.getStats().getCurrentHp());
    }

    @Test
    public void smallerPartiesFaceWeakerEnemies() {
        Dungeon dungeon = Dungeon.standard();
        assertEquals(0.75, Dungeon.partySizeScale(1), 0.0001);
        assertEquals(0.90, Dungeon.partySizeScale(2), 0.0001);
        assertEquals(1.00, Dungeon.partySizeScale(3), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE, dungeon.scaleForWave(1, 3), 0.0001);
        assertEquals(Dungeon.STARTING_SCALE * 0.75, dungeon.scaleForWave(1, 1), 0.0001);
        double blockTwo = Dungeon.STARTING_SCALE + Dungeon.SCALE_PER_BLOCK;
        assertEquals(blockTwo * 0.75, dungeon.scaleForWave(16, 1), 0.0001);
        assertEquals(blockTwo * 0.90, dungeon.scaleForWave(16, 2), 0.0001);
        assertEquals(blockTwo, dungeon.scaleForWave(16, 3), 0.0001);
    }

    @Test
    public void runClearsEasyWavesAndStopsOnAWipe() {
        EnemyTemplate wall = unbeatableWall();
        Dungeon dungeon = new Dungeon(List.of(
                new Wave(List.of(Enemies.goblin())),
                new Wave(List.of(wall))), 0.15);
        Character knight = PlayableCharacters.knight().createInstance();
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);

        DungeonResult result = DungeonRun.run(List.of(knight), dungeon, ai, random);

        assertEquals(1, result.getWavesCleared());
        assertFalse(result.clearedAll());
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("--- Wave 1 ---")));
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("--- Wave 2 ---")));
    }

    @Test
    public void runReportsAFullClear() {
        Dungeon dungeon = new Dungeon(List.of(
                new Wave(List.of(Enemies.iceSlime())),
                new Wave(List.of(Enemies.goblin()))), 0.15);
        Character knight = PlayableCharacters.knight().createInstance();
        RandomProvider random = new RandomProvider(2L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);

        DungeonResult result = DungeonRun.run(List.of(knight), dungeon, ai, random);

        assertTrue(result.clearedAll());
        assertEquals(2, result.getWavesCleared());
    }

    @Test
    public void soloDifficultyCutsWaveXp() {
        Dungeon dungeon = new Dungeon(List.of(new Wave(List.of(Enemies.goblin()))), 0.15);
        Character knight = PlayableCharacters.knight().createInstance();
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);

        DungeonResult result = DungeonRun.run(
                List.of(knight), dungeon, ai, random, false, false, character -> { });

        assertTrue(result.clearedAll());
        assertEquals(1, knight.getLevel());
        assertEquals(75, knight.getXp());
    }

    @Test
    public void trioGetsFullXpAndLevelsFromANormal() {
        Dungeon dungeon = new Dungeon(List.of(new Wave(List.of(Enemies.iceSlime()))), 0.15);
        Character knight = PlayableCharacters.knight().createInstance();
        Character rogue = PlayableCharacters.rogue().createInstance();
        Character randy = PlayableCharacters.caveman().createInstance();
        int startHp = knight.getStats().getMaxHp();
        RandomProvider random = new RandomProvider(3L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);

        DungeonResult result = DungeonRun.run(
                List.of(knight, rogue, randy), dungeon, ai, random, true, false, character -> { });

        assertTrue(result.clearedAll());
        assertEquals(2, knight.getLevel());
        assertEquals(0, knight.getXp());
        assertEquals(startHp + Growth.AUTO_HP_PER_LEVEL, knight.getStats().getMaxHp());
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("gained 100 XP")));
    }

    @Test
    public void clearedWavesGrantGoldIntoTheBag() {
        Dungeon dungeon = new Dungeon(List.of(new Wave(List.of(Enemies.goblin()))), 0.15);
        Character knight = PlayableCharacters.knight().createInstance();
        Inventory bag = new Inventory();
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);

        DungeonResult result = DungeonRun.run(
                List.of(knight), dungeon, ai, random, false, false, character -> { },
                bag, Camp.NONE);

        assertTrue(result.clearedAll());
        assertEquals(DropTable.goldFor(EnemyRank.NORMAL, Dungeon.partySizeScale(1)), bag.getGold());
    }

    private static EnemyTemplate unbeatableWall() {
        Move crush = new Move("Crush", Type.PHYSICAL, 200, 100, 0, false, Status.NONE, 0);
        CharacterTemplate body = new CharacterTemplate(
                "Wall", 9999, 200, 1, 1, 1, 80,
                Type.PHYSICAL, List.of(crush));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }
}
