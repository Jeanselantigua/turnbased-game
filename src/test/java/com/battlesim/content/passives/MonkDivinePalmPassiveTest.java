package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
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
import java.util.List;
import org.junit.Test;

public class MonkDivinePalmPassiveTest {

    @Test
    public void divinePalmSplashesFortyPercentOntoAdjacentEnemies() {
        Move palm = new Move(MonkPerfectEnlightenmentPassive.DIVINE_PALM_NAME, Type.HOLY, 40, 100, 0, true,
                Status.NONE, 0);
        Character monk = new Character("Roeseph", new Stats(200, 20, 10, 40, 10, 15),
                Type.HOLY, List.of(palm), List.of(new MonkHolySplitPassive(), new MonkDivinePalmPassive()));
        Character left = dummy("Left");
        Character mid = dummy("Mid");
        Character right = dummy("Right");
        BattleContext context = field(monk, List.of(left, mid, right));

        alwaysHits().resolveAction(monk, new ActionChoice(palm, List.of(mid)), context);

        int midLost = 400 - mid.getStats().getCurrentHp();
        int expectedSplash = (int) Math.round(midLost * MonkDivinePalmPassive.SPLASH_PERCENT);
        assertEquals(expectedSplash, 400 - left.getStats().getCurrentHp());
        assertEquals(expectedSplash, 400 - right.getStats().getCurrentHp());
    }

    private static Character dummy(String name) {
        return new Character(name, new Stats(400, 10, 10, 10, 10, 10), Type.PHYSICAL, List.of());
    }

    private static BattleContext field(Character monk, List<Character> foes) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == monk ? List.of(monk) : foes;
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == monk ? foes : List.of(monk);
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
