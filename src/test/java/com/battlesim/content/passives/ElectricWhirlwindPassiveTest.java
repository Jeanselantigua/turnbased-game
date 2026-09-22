package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.Character;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class ElectricWhirlwindPassiveTest {

    private static TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    @Test
    public void dualSwordsmanTemplateHasTheUlt() {
        Character dualist = PlayableCharacters.dualSwordsman().createFullyLearnedInstance();
        assertTrue(dualist.hasPassive(ElectricWhirlwindPassive.class));
        assertEquals(ElectricWhirlwindPassive.MOVE_NAME, dualist.getKit().getUlt().getName());
    }

    @Test
    public void whirlwindAppliesParalysisAndBleed() {
        Character dualist = new Character("Dual Swordsman",
                new Stats(200, 40, 20, 40, 20, 30), Type.LIGHTNING,
                List.of(ElectricWhirlwindPassive.createMove()),
                List.of(new ElectricWhirlwindPassive(), new DualSwordsmanStatusRerollPassive()));
        Character dummy = new Character("Dummy", new Stats(800, 1, 10, 1, 10, 10),
                Type.PHYSICAL, List.of());

        List<String> log = alwaysHits().resolveAction(dualist,
                new ActionChoice(ElectricWhirlwindPassive.createMove(), List.of(dummy)));

        assertTrue(dummy.hasStatus(Status.PARALYSIS));
        assertTrue(dummy.hasStatus(Status.BLEED));
        assertTrue(log.stream().anyMatch(line -> line.contains("rips with steel")));
        assertTrue(log.stream().anyMatch(line -> line.contains("is now PARALYSIS")));
        assertTrue(log.stream().anyMatch(line -> line.contains("is now BLEED")));
    }
}
