package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Team;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class BattleSimTest {

    @Test
    public void healOnlyAiVsAiStopsAtMaxActions() {
        Move heal = new Move("Heal", Type.HOLY, 0, 100, 0, true, Status.HEAL, 100);
        Character a = new Character("HealerA", new Stats(175, 10, 10, 57, 25, 32), Type.HOLY, List.of(heal));
        Character b = new Character("HealerB", new Stats(175, 10, 10, 57, 25, 32), Type.HOLY, List.of(heal));
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);
        int maxActions = 30;

        BattleResult result = Battle.create(
                new Team(List.of(a)), new Team(List.of(b)),
                ai, ai, random, maxActions, false).run();

        assertEquals(BattleResult.Winner.TIMEOUT, result.getWinner());
        assertEquals(maxActions, result.getActionCount());
        assertTrue(a.getStats().getCurrentHp() > 0);
        assertTrue(b.getStats().getCurrentHp() > 0);
    }

    @Test
    public void damagingAiVsAiFinishesWithoutHanging() {
        Character a = PlayableCharacters.knight().createInstance();
        Character b = PlayableCharacters.rogue().createInstance();
        RandomProvider random = new RandomProvider(42L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);
        int maxActions = 200;

        BattleResult result = Battle.create(
                new Team(List.of(a)), new Team(List.of(b)),
                ai, ai, random, maxActions, false).run();

        assertNotNull(result.getWinner());
        assertTrue(result.getActionCount() > 0);
        assertTrue(result.getActionCount() <= maxActions);
        assertFalse(result.getFighters().isEmpty());
        if (result.getWinner() != BattleResult.Winner.TIMEOUT) {
            boolean aAlive = result.getFighters().stream()
                    .anyMatch(f -> "A".equals(f.getTeamId()) && !f.isFainted());
            boolean bAlive = result.getFighters().stream()
                    .anyMatch(f -> "B".equals(f.getTeamId()) && !f.isFainted());
            if (result.getWinner() == BattleResult.Winner.TEAM_A) {
                assertTrue(aAlive);
                assertFalse(bAlive);
            } else if (result.getWinner() == BattleResult.Winner.TEAM_B) {
                assertTrue(bAlive);
                assertFalse(aAlive);
            }
        }
    }
}
