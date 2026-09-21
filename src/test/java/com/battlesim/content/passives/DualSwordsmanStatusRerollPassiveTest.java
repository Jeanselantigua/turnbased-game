package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
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

public class DualSwordsmanStatusRerollPassiveTest {

    @Test
    public void dualSwordsmanTemplateHasStatusRerollPassive() {
        Character dualist = PlayableCharacters.dualSwordsman().createInstance();
        assertTrue(dualist.hasPassive(DualSwordsmanStatusRerollPassive.class));
    }

    @Test
    public void procsRerollWhenRollSucceeds() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive(always(0.0));
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Move slash = new Move("Diagonal Strike", Type.PHYSICAL, 40, 90, 0, false, Status.BLEED, 30);
        List<String> log = new ArrayList<>();

        assertTrue(passive.shouldRerollFailedStatus(self, target, slash, log));
        assertTrue(log.stream().anyMatch(line -> line.contains("presses the status")));
    }

    @Test
    public void doesNotRerollWhenRollFails() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive(always(0.99));
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Move slash = new Move("Diagonal Strike", Type.PHYSICAL, 40, 90, 0, false, Status.BLEED, 30);

        assertFalse(passive.shouldRerollFailedStatus(self, target, slash, new ArrayList<>()));
    }

    @Test
    public void doesNotRerollMovesWithNoStatus() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive(always(0.0));
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Move slash = new Move("Slash", Type.PHYSICAL, 40, 90, 0, false, Status.NONE, 0);

        assertFalse(passive.shouldRerollFailedStatus(self, target, slash, new ArrayList<>()));
    }

    @Test
    public void doublesBleedMagnitude() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive();
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        List<String> log = new ArrayList<>();

        assertEquals(100, passive.modifyOutgoingStatusMagnitude(self, target, Status.BLEED, 50, log));
        assertTrue(log.stream().anyMatch(line -> line.contains("twice as deep")));
    }

    @Test
    public void paralysisSkipLastsTwoTurns() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive();
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        List<String> log = new ArrayList<>();

        assertEquals(2, passive.modifyOutgoingStatusMagnitude(self, target, Status.PARALYSIS, 0, log));
        assertTrue(log.stream().anyMatch(line -> line.contains("seize for 2 turns")));
    }

    @Test
    public void doesNotAmplifyOtherStatuses() {
        DualSwordsmanStatusRerollPassive passive = new DualSwordsmanStatusRerollPassive();
        Character self = character(passive);
        Character target = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());

        assertEquals(0, passive.modifyOutgoingStatusMagnitude(self, target, Status.SLOW, 0, new ArrayList<>()));
    }

    private static Character character(DualSwordsmanStatusRerollPassive passive) {
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
