package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.StatusEffectResolver;
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

public class RogueWoundPassiveTest {

    private static TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static Character rogue() {
        Move maim = RogueWoundPassive.createMaim();
        Move ult = RogueWoundPassive.createAssassinate();
        return new Character("Rogue", new Stats(200, 40, 10, 10, 10, 70),
                Type.ARCANE, List.of(maim, ult), List.of(new RogueWoundPassive()));
    }

    private static Character dummy(int maxHp) {
        return new Character("Dummy", new Stats(maxHp, 1, 10, 1, 10, 10),
                Type.PHYSICAL, List.of());
    }

    @Test
    public void rogueTemplateHasWoundPassiveAndMaim() {
        Character rogue = PlayableCharacters.rogue().createFullyLearnedInstance();
        assertTrue(rogue.hasPassive(RogueWoundPassive.class));
        assertTrue(rogue.knowsMove(RogueWoundPassive.MAIM_NAME));
        assertEquals(RogueWoundPassive.ASSASSINATE_COOLDOWN,
                rogue.getMoveByName(RogueWoundPassive.ASSASSINATE_NAME).getCooldownTurns());
    }

    @Test
    public void maimHitsSeveralTimesAndStacksWounds() {
        Character rogue = rogue();
        Character dummy = dummy(500);
        List<String> log = alwaysHits().resolveAction(rogue,
                new ActionChoice(RogueWoundPassive.createMaim(), List.of(dummy)));

        assertTrue(log.stream().anyMatch(line -> line.contains("strikes 3 times")));
        assertEquals(3, dummy.getWoundStacks());
        assertEquals(2, dummy.getWoundTurnsRemaining());
        assertEquals(Status.NONE, dummy.getStatus());
        assertTrue(log.stream().noneMatch(line -> line.toLowerCase().contains("damage from wound")));
    }

    @Test
    public void woundStacksCapAtSix() {
        Character dummy = dummy(200);
        dummy.addWoundStacks(10, 2);
        assertEquals(Character.WOUND_STACK_CAP, dummy.getWoundStacks());
        dummy.addWoundStacks(1, 2);
        assertEquals(Character.WOUND_STACK_CAP, dummy.getWoundStacks());
    }

    @Test
    public void woundExpiresAfterTwoOfTheVictimsTurnsWithoutTicking() {
        Character dummy = dummy(200);
        dummy.addWoundStacks(3, 2);
        int hp = dummy.getStats().getCurrentHp();
        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new java.util.ArrayList<>();

        resolver.applyStartOfTurnEffects(dummy, log);
        assertEquals(3, dummy.getWoundStacks());
        assertEquals(1, dummy.getWoundTurnsRemaining());
        assertEquals(hp, dummy.getStats().getCurrentHp());

        resolver.applyStartOfTurnEffects(dummy, log);
        assertEquals(0, dummy.getWoundStacks());
        assertFalse(dummy.isWounded());
        assertEquals(hp, dummy.getStats().getCurrentHp());
    }

    @Test
    public void assassinateDetonatesWoundsAndStartsAShortCooldown() {
        Character rogue = rogue();
        Character dummy = dummy(2000);
        dummy.addWoundStacks(3, 2);
        int expectedBurst = RogueWoundPassive.detonationDamage(3, 2000);
        int before = dummy.getStats().getCurrentHp();

        List<String> log = alwaysHits().resolveAction(rogue,
                new ActionChoice(RogueWoundPassive.createAssassinate(), List.of(dummy)));

        assertEquals(0, dummy.getWoundStacks());
        assertTrue(log.stream().anyMatch(line -> line.contains("triggers 3 wound stacks")));
        assertTrue(before - dummy.getStats().getCurrentHp() >= expectedBurst);
        assertEquals(RogueWoundPassive.ASSASSINATE_COOLDOWN,
                rogue.getMoveCooldown(RogueWoundPassive.ASSASSINATE_NAME));
    }
}
