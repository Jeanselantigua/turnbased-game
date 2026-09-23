package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SionSlamPassiveTest {

    @Test
    public void slamWindsUpThenHitsOnTheSkippedTurn() {
        SionSlamPassive slam = new SionSlamPassive();
        Move smash = new Move(SionSlamPassive.MOVE_NAME, Type.PHYSICAL, 45, 100, 0, false,
                Status.STUN, 100);
        Character sion = new Character("Sion", new Stats(270, 40, 20, 10, 20, 10),
                Type.LIGHTNING, List.of(smash), List.of(slam));
        Character foe = new Character("Foe", new Stats(400, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        BattleContext context = twoFighters(sion, foe);
        TurnResolver resolver = alwaysHits();

        List<String> windup = resolver.resolveAction(sion, new ActionChoice(smash, List.of(foe)), context);
        assertEquals(400, foe.getStats().getCurrentHp());
        assertTrue(slam.skipsOwnAction(sion));
        assertTrue(windup.stream().anyMatch(line -> line.contains("charging")));

        List<String> skipLog = new ArrayList<>();
        slam.onActionSkipped(sion, context, skipLog);
        assertTrue(sion.hasQueuedAction());
        assertFalse(slam.skipsOwnAction(sion));

        resolver.resolveAction(sion, new ActionChoice(sion.consumeQueuedMove(), sion.consumeQueuedTargets()),
                context);
        assertTrue(foe.getStats().getCurrentHp() < 400);
        assertTrue(foe.hasStatus(Status.STUN));
    }

    private static BattleContext twoFighters(Character sion, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == foe ? List.of(foe) : List.of(sion);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == foe ? List.of(sion) : List.of(foe);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }

    private static TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }
}
