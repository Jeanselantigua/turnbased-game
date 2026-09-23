package com.battlesim.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Team;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class BattleObserverTest {

    @Test
    public void observerReceivesLogAndFieldWhenVerboseIsOff() {
        Move slash = new Move("Slash", Type.PHYSICAL, 80, 100, 0, false, Status.NONE, 0);
        Character hero = new Character("Hero", new Stats(200, 40, 10, 1, 10, 40), Type.PHYSICAL, List.of(slash));
        Character foe = new Character("Foe", new Stats(80, 10, 5, 1, 5, 10), Type.PHYSICAL, List.of(slash));
        RandomProvider random = new RandomProvider(1L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);
        List<String> lines = new ArrayList<>();
        List<Integer> fieldSizes = new ArrayList<>();
        List<Integer> queueSizes = new ArrayList<>();

        Battle.create(new Team(List.of(hero)), new Team(List.of(foe)),
                ai, ai, random, 20, false)
                .withObserver(new BattleObserver() {
                    @Override
                    public void onLog(List<String> batch) {
                        lines.addAll(batch);
                    }

                    @Override
                    public void onField(List<Character> teamA, List<Character> teamB) {
                        fieldSizes.add(teamA.size() + teamB.size());
                    }

                    @Override
                    public void onTurnQueue(List<TurnOrderScheduler.UpcomingTurn> upcoming,
                                           List<Character> allies) {
                        queueSizes.add(upcoming.size());
                    }
                })
                .run();

        assertFalse(lines.isEmpty());
        assertTrue(fieldSizes.stream().anyMatch(size -> size >= 2));
        assertTrue(queueSizes.stream().anyMatch(size -> size >= 1));
    }
}
