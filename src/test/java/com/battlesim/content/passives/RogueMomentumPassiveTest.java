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

public class RogueMomentumPassiveTest {

    private static final int BASE_ATTACK = 60;
    private static final int BASE_SPEED = 70;

    @Test
    public void rogueTemplateHasMomentumPassive() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        assertTrue(rogue.hasPassive(RogueMomentumPassive.class));
    }

    @Test
    public void killGrantsSpeedAndAttack() {
        RogueMomentumPassive passive = new RogueMomentumPassive();
        Character rogue = rogue(passive);
        List<String> log = new ArrayList<>();

        kill(rogue, log);

        assertEquals(BASE_ATTACK + RogueMomentumPassive.ATTACK_PER_STACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED + RogueMomentumPassive.SPEED_PER_STACK, rogue.getStats().getSpeed());
        assertTrue(log.stream().anyMatch(line -> line.contains("gains Momentum") && line.contains("1 stack")));
    }

    @Test
    public void killsStack() {
        RogueMomentumPassive passive = new RogueMomentumPassive();
        Character rogue = rogue(passive);

        kill(rogue, new ArrayList<>());
        kill(rogue, new ArrayList<>());

        assertEquals(BASE_ATTACK + 2 * RogueMomentumPassive.ATTACK_PER_STACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED + 2 * RogueMomentumPassive.SPEED_PER_STACK, rogue.getStats().getSpeed());
    }

    @Test
    public void threeTurnsWithoutKillDropsAllStacks() {
        RogueMomentumPassive passive = new RogueMomentumPassive();
        Character rogue = rogue(passive);
        List<String> log = new ArrayList<>();

        kill(rogue, log);
        kill(rogue, log);
        startTurns(passive, rogue, 2, log);

        assertEquals(BASE_ATTACK + 2 * RogueMomentumPassive.ATTACK_PER_STACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED + 2 * RogueMomentumPassive.SPEED_PER_STACK, rogue.getStats().getSpeed());

        startTurns(passive, rogue, 1, log);

        assertEquals(BASE_ATTACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED, rogue.getStats().getSpeed());
        assertTrue(log.stream().anyMatch(line -> line.contains("Momentum fades")));
    }

    @Test
    public void aKillResetsTheIdleTimer() {
        RogueMomentumPassive passive = new RogueMomentumPassive();
        Character rogue = rogue(passive);

        kill(rogue, new ArrayList<>());
        startTurns(passive, rogue, 2, new ArrayList<>());
        kill(rogue, new ArrayList<>());
        startTurns(passive, rogue, 2, new ArrayList<>());

        assertEquals(BASE_ATTACK + 2 * RogueMomentumPassive.ATTACK_PER_STACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED + 2 * RogueMomentumPassive.SPEED_PER_STACK, rogue.getStats().getSpeed());

        startTurns(passive, rogue, 1, new ArrayList<>());

        assertEquals(BASE_ATTACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED, rogue.getStats().getSpeed());
    }

    @Test
    public void faintedKillerDoesNotGainMomentum() {
        RogueMomentumPassive passive = new RogueMomentumPassive();
        Character rogue = rogue(passive);
        rogue.getStats().applyDamage(200);

        kill(rogue, new ArrayList<>());

        assertEquals(BASE_ATTACK, rogue.getStats().getAttack());
        assertEquals(BASE_SPEED, rogue.getStats().getSpeed());
    }

    private static Character rogue(RogueMomentumPassive passive) {
        return new Character("Rogue", new Stats(200, BASE_ATTACK, 20, 20, 30, BASE_SPEED),
                Type.ARCANE, List.of(), List.of(passive));
    }

    private static void kill(Character rogue, List<String> log) {
        Character foe = new Character("Foe", new Stats(10, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        foe.getStats().applyDamage(10);
        new StatusEffectResolver().notifyFaint(foe, rogue, null, null, log);
    }

    private static void startTurns(RogueMomentumPassive passive, Character rogue,
                                   int count, List<String> log) {
        for (int i = 0; i < count; i++) {
            passive.onTurnStart(rogue, null, log);
        }
    }
}
