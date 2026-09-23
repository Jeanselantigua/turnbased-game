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
import org.junit.Test;

public class WomanBlessingPassiveTest {

    @Test
    public void okirikTemplateHasBlessingPassive() {
        Character okirik = PlayableCharacters.healer().createInstance();
        assertTrue(okirik.hasPassive(WomanBlessingPassive.class));
    }

    @Test
    public void healCleansesDebuffsSiphonAndWounds() {
        WomanBlessingPassive passive = new WomanBlessingPassive();
        Character healer = healer(passive);
        Character ally = character("Ally", 200);
        ally.setStatus(Status.BLEED, 80);
        ally.setStatus(Status.PARALYSIS);
        ally.setStatus(Status.SLOW);
        ally.applySiphon(healer, 3);
        ally.addWoundStacks(2, 2);
        List<String> log = new ArrayList<>();

        passive.onAllyHealed(healer, ally, 20, log);

        assertFalse(ally.hasStatus(Status.BLEED));
        assertFalse(ally.hasStatus(Status.PARALYSIS));
        assertFalse(ally.hasStatus(Status.SLOW));
        assertFalse(ally.isSiphoned());
        assertEquals(0, ally.getWoundStacks());
        assertTrue(log.stream().anyMatch(line -> line.contains("cleansed of harmful statuses")));
        assertTrue(log.stream().anyMatch(line -> line.contains("will take less damage")));
    }

    @Test
    public void fullHpHealStillCleanses() {
        WomanBlessingPassive passive = new WomanBlessingPassive();
        Character healer = healer(passive);
        Character ally = character("Ally", 200);
        ally.setStatus(Status.PARALYSIS);
        List<String> log = new ArrayList<>();

        passive.onAllyHealed(healer, ally, 0, log);

        assertFalse(ally.hasStatus(Status.PARALYSIS));
        assertTrue(log.stream().anyMatch(line -> line.contains("cleansed")));
        assertFalse(log.stream().anyMatch(line -> line.contains("will take less damage")));
    }

    @Test
    public void blessingStillReducesTheNextHit() {
        WomanBlessingPassive passive = new WomanBlessingPassive();
        Character healer = healer(passive);
        Character ally = character("Ally", 200);
        Character attacker = character("Foe", 200);
        Move slash = new Move("Slash", Type.PHYSICAL, 40, 100, 0, false, Status.NONE, 0);
        List<String> log = new ArrayList<>();

        passive.onAllyHealed(healer, ally, 15, log);
        double reduced = passive.modifyIncomingDamageToAlly(healer, ally, attacker, slash, 100, log);

        assertEquals(60.0, reduced, 0.0001);
        assertTrue(log.stream().anyMatch(line -> line.contains("softens the blow")));
    }

    private static Character healer(WomanBlessingPassive passive) {
        return new Character("Okirik", new Stats(200, 10, 20, 60, 40, 32),
                Type.HOLY, List.of(), List.of(passive));
    }

    private static Character character(String name, int maxHp) {
        return new Character(name, new Stats(maxHp, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
    }
}
