package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.StatusEffectResolver;
import com.battlesim.model.Character;
import com.battlesim.model.Stats;
import com.battlesim.model.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SionKillMaxHpPassiveTest {

    @Test
    public void sionTemplateHasKillMaxHpPassive() {
        Character sion = PlayableCharacters.sion().createInstance();
        assertTrue(sion.hasPassive(SionKillMaxHpPassive.class));
    }

    @Test
    public void killRaisesMaxHpAndKeepsCurrentPercent() {
        SionKillMaxHpPassive passive = new SionKillMaxHpPassive();
        Character sion = new Character("Sion", new Stats(200, 10, 10, 10, 10, 10),
                Type.LIGHTNING, List.of(), List.of(passive));
        sion.getStats().applyDamage(100);
        Character foe = new Character("Foe", new Stats(10, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        foe.getStats().applyDamage(10);

        List<String> log = new ArrayList<>();
        new StatusEffectResolver().notifyFaint(foe, sion, null, null, log);

        assertEquals(200 + SionKillMaxHpPassive.HP_PER_KILL, sion.getStats().getMaxHp());
        assertEquals(120, sion.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("max HP is now 240")));
    }

    @Test
    public void killAtFullHpStaysAtFullHp() {
        SionKillMaxHpPassive passive = new SionKillMaxHpPassive();
        Character sion = new Character("Sion", new Stats(200, 10, 10, 10, 10, 10),
                Type.LIGHTNING, List.of(), List.of(passive));
        Character foe = new Character("Foe", new Stats(10, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        foe.getStats().applyDamage(10);

        new StatusEffectResolver().notifyFaint(foe, sion, null, null, new ArrayList<>());

        assertEquals(240, sion.getStats().getMaxHp());
        assertEquals(240, sion.getStats().getCurrentHp());
    }

    @Test
    public void faintedKillerDoesNotGainMaxHp() {
        SionKillMaxHpPassive passive = new SionKillMaxHpPassive();
        Character sion = new Character("Sion", new Stats(200, 10, 10, 10, 10, 10),
                Type.LIGHTNING, List.of(), List.of(passive));
        sion.getStats().applyDamage(200);
        Character foe = new Character("Foe", new Stats(10, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        foe.getStats().applyDamage(10);

        new StatusEffectResolver().notifyFaint(foe, sion, null, null, new ArrayList<>());

        assertEquals(200, sion.getStats().getMaxHp());
        assertEquals(0, sion.getStats().getCurrentHp());
    }
}
