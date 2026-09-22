package com.battlesim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.Enemies;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.dungeon.Dungeon;
import com.battlesim.dungeon.Wave;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class BalanceSimDungeonTest {

    @Test
    public void climbableKeepsEveryPlayableWithMoves() {
        List<CharacterTemplate> climbable = BalanceSim.climbable(PlayableCharacters.all());
        assertEquals(PlayableCharacters.all().size(), climbable.size());
    }

    @Test
    public void medianPicksTheMiddleSample() {
        assertEquals(2.0, BalanceSim.median(new int[] {1, 2, 3}), 0.0001);
        assertEquals(2.5, BalanceSim.median(new int[] {1, 2, 3, 4}), 0.0001);
    }

    @Test
    public void knightClearsASingleGoblinWave() {
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);
        Dungeon oneWave = new Dungeon(List.of(new Wave(List.of(Enemies.goblin()))), 0.15);

        BalanceSim.ClimbRow row = BalanceSim.measureClimb(
                List.of(PlayableCharacters.knight()), 3, ai, random, () -> oneWave);

        assertEquals(1.0, row.avg, 0.0001);
        assertEquals(1.0, row.median, 0.0001);
        assertEquals(1, row.min);
        assertEquals(1, row.max);
        assertEquals(100.0, row.clearPct, 0.0001);
        assertTrue(row.name.contains("Mechanized Champion"));
    }

    @Test
    public void teamMatchesAreThreeOnThree() {
        assertEquals(3, BalanceSim.TEAM_SIZE);
    }

    @Test
    public void mostLopsidedKeepsTheBiggestSpreads() {
        BalanceSim.SplitRow close = new BalanceSim.SplitRow("A", "B", 26, 24, 0, 0, 50);
        BalanceSim.SplitRow blowout = new BalanceSim.SplitRow("C", "D", 50, 0, 0, 0, 50);
        BalanceSim.SplitRow mild = new BalanceSim.SplitRow("E", "F", 30, 20, 0, 0, 50);
        List<BalanceSim.SplitRow> shown = BalanceSim.mostLopsided(
                List.of(close, blowout, mild), 2);
        assertEquals(2, shown.size());
        assertEquals("C", shown.get(0).teamA);
        assertEquals("E", shown.get(1).teamA);
    }
}
