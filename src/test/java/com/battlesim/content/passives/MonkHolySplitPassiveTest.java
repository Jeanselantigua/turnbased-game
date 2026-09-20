package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class MonkHolySplitPassiveTest {

    private TurnResolver alwaysHitsLowVariance() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private Character monk() {
        Stats stats = new Stats(500, 40, 10, 100, 10, 10);
        Move oochie = new Move("Oochie", Type.HOLY, 50, 100, 0, false, Status.NONE, 0);
        return new Character("Roeseph", stats, Type.PHYSICAL, List.of(oochie),
                List.of(new MonkHolySplitPassive()));
    }

    private Character dummy(String name, int defense, int magicDefense) {
        Stats stats = new Stats(500, 10, defense, 10, magicDefense, 10);
        return new Character(name, stats, Type.PHYSICAL, List.of());
    }

    @Test
    public void holyBonusIgnoresTargetDefense() {
        Character monk = monk();
        Character frail = dummy("Frail", 10, 10);
        Character tank = dummy("Tank", 1000, 1000);
        Move oochie = monk.getMoveByName("Oochie");
        TurnResolver resolver = alwaysHitsLowVariance();

        resolver.resolveAction(monk, new ActionChoice(oochie, List.of(frail)));
        resolver.resolveAction(monk, new ActionChoice(oochie, List.of(tank)));

        int bonus = (int) Math.round(100 * 0.60);
        int frailLost = 500 - frail.getStats().getCurrentHp();
        int tankLost = 500 - tank.getStats().getCurrentHp();

        assertEquals(230, frailLost);
        assertEquals(2 + bonus, tankLost);
        assertTrue(tankLost >= bonus);
    }
}
