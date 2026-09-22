package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class HeavenPiercingBladePassiveTest {

    private static TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static TurnResolver alwaysMisses() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return max;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static Character healthyChampion() {
        return PlayableCharacters.knight().createFullyLearnedInstance();
    }

    private static Character hailMaryChampion() {
        Character champion = PlayableCharacters.knight().createFullyLearnedInstance();
        int max = champion.getStats().getMaxHp();
        int threshold = (max * 3) / 20;
        champion.getStats().applyDamage(max - Math.max(1, threshold - 1));
        return champion;
    }

    private static Character dummy(int hp) {
        return new Character("Dummy", new Stats(hp, 1, 10, 1, 10, 10),
                Type.PHYSICAL, List.of());
    }

    @Test
    public void ultIsLockedUntilBelowFifteenPercentHp() {
        HeavenPiercingBladePassive hailMary = new HeavenPiercingBladePassive();
        Character healthy = healthyChampion();
        List<Move> hidden = hailMary.filterOwnMoves(healthy, healthy.getMoves(), null);
        assertFalse(hidden.stream().anyMatch(move -> HeavenPiercingBladePassive.MOVE_NAME.equals(move.getName())));

        Character desperate = hailMaryChampion();
        List<Move> unlocked = hailMary.filterOwnMoves(desperate, desperate.getMoves(), null);
        assertTrue(unlocked.stream().anyMatch(move -> HeavenPiercingBladePassive.MOVE_NAME.equals(move.getName())));
        assertEquals(20, desperate.getMoveByName(HeavenPiercingBladePassive.MOVE_NAME).getAccuracy());
        assertEquals(Growth.ULT_COOLDOWN_TURNS,
                desperate.getMoveByName(HeavenPiercingBladePassive.MOVE_NAME).getCooldownTurns());
    }

    @Test
    public void connectedHailMaryInstantlyKills() {
        Character champion = hailMaryChampion();
        Character dummy = dummy(500);
        Move ult = HeavenPiercingBladePassive.createMove();
        List<String> log = alwaysHits().resolveAction(champion, new ActionChoice(ult, List.of(dummy)));

        assertTrue(dummy.isFainted());
        assertEquals(0, dummy.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("fabric of the world")));
    }

    @Test
    public void missedHailMaryDoesNotKill() {
        Character champion = hailMaryChampion();
        Character dummy = dummy(500);
        Move ult = HeavenPiercingBladePassive.createMove();
        alwaysMisses().resolveAction(champion, new ActionChoice(ult, List.of(dummy)));

        assertFalse(dummy.isFainted());
        assertEquals(500, dummy.getStats().getCurrentHp());
    }

    @Test
    public void aiUsesHailMaryWhenItIsAvailable() {
        Character champion = hailMaryChampion();
        Character foe = dummy(400);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));
        HeavenPiercingBladePassive hailMary = champion.getPassive(HeavenPiercingBladePassive.class);
        List<Move> available = hailMary.filterOwnMoves(champion, champion.getMoves(), null);

        ActionChoice choice = ai.chooseAction(champion, available, List.of(foe), List.of(champion));
        assertEquals(HeavenPiercingBladePassive.MOVE_NAME, choice.getMove().getName());
        assertEquals(foe, choice.getTargets().get(0));
    }
}
