package com.battlesim.content.passives;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class DualSwordsmanDoubleHitPassiveTest {

    @Test
    public void dualSwordsmanTemplateHasDoubleHitPassive() {
        Character dualist = PlayableCharacters.dualSwordsman().createInstance();
        assertTrue(dualist.hasPassive(DualSwordsmanDoubleHitPassive.class));
    }

    @Test
    public void procsRepeatWhenRollSucceeds() {
        DualSwordsmanDoubleHitPassive passive = new DualSwordsmanDoubleHitPassive(always(0.0));
        Character self = character(passive);
        Move slash = new Move("Diagonal Strike", Type.PHYSICAL, 40, 90, 0, false, Status.BLEED, 30);
        List<String> log = new ArrayList<>();

        assertTrue(passive.shouldRepeatAction(self, slash, List.of(), null, log));
        assertTrue(log.stream().anyMatch(line -> line.contains("strikes again")));
    }

    @Test
    public void doesNotRepeatWhenRollFails() {
        DualSwordsmanDoubleHitPassive passive = new DualSwordsmanDoubleHitPassive(always(0.99));
        Character self = character(passive);
        Move slash = new Move("Diagonal Strike", Type.PHYSICAL, 40, 90, 0, false, Status.BLEED, 30);

        assertFalse(passive.shouldRepeatAction(self, slash, List.of(), null, new ArrayList<>()));
    }

    private static Character character(DualSwordsmanDoubleHitPassive passive) {
        return new Character("Dual Swordsman", new Stats(180, 50, 50, 60, 30, 30),
                Type.LIGHTNING, List.of(), List.of(passive));
    }

    private static Random always(double value) {
        return new Random() {
            @Override
            public double nextDouble() {
                return value;
            }
        };
    }
}
