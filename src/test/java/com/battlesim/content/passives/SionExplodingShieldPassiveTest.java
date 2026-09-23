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
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SionExplodingShieldPassiveTest {

    @Test
    public void sionTemplateHasRoarAndShieldPassive() {
        Character sion = PlayableCharacters.sion().createFullyLearnedInstance();
        assertTrue(sion.hasPassive(SionExplodingShieldPassive.class));
        Move roar = sion.getMoveByName(SionExplodingShieldPassive.MOVE_NAME);
        assertEquals(Status.SELF_SHIELD, roar.getInflictedStatus());
        assertEquals(SionExplodingShieldPassive.SHIELD_HP, roar.getPower());
        assertTrue(roar.targetsSelfOnly());
    }

    @Test
    public void roarThroughTurnResolverGrantsShieldToTheTarget() {
        SionExplodingShieldPassive passive = new SionExplodingShieldPassive();
        Character sion = sionWith(passive);
        Character foe = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        TurnResolver resolver = alwaysHits();

        List<String> log = resolver.resolveAction(sion,
                new ActionChoice(SionExplodingShieldPassive.createMove(), List.of(sion)),
                twoFighters(sion, foe));

        assertEquals(SionExplodingShieldPassive.SHIELD_HP, sion.getStats().getShieldHp());
        assertEquals(270, sion.getStats().getCurrentHp());
        assertEquals(200, foe.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("100 HP shield")));
    }

    @Test
    public void roarShieldsSionEvenIfAnAllyIsPassedAsTarget() {
        Character sion = sionWith(new SionExplodingShieldPassive());
        Character ally = new Character("Ally", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        alwaysHits().resolveAction(sion,
                new ActionChoice(SionExplodingShieldPassive.createMove(), List.of(ally)));

        assertEquals(SionExplodingShieldPassive.SHIELD_HP, sion.getStats().getShieldHp());
        assertEquals(0, ally.getStats().getShieldHp());
    }

    @Test
    public void shieldAbsorbsDamageUntilItBreaksThenExplodes() {
        SionExplodingShieldPassive passive = new SionExplodingShieldPassive();
        Character sion = sionWith(passive);
        Character foe = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        BattleContext context = twoFighters(sion, foe);
        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();

        resolver.applyShield(sion, SionExplodingShieldPassive.SHIELD_HP, log);

        int hpLost = resolver.applyDamageThroughShield(sion, foe, 40, context, log);
        assertEquals(0, hpLost);
        assertEquals(60, sion.getStats().getShieldHp());
        assertEquals(270, sion.getStats().getCurrentHp());

        hpLost = resolver.applyDamageThroughShield(sion, foe, 80, context, log);
        assertEquals(20, hpLost);
        assertEquals(0, sion.getStats().getShieldHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("shatters")));

        passive.onFieldChanged(sion, context, log);
        int expected = 200 - SionExplodingShieldPassive.explosionDamage(sion);
        assertEquals(expected, foe.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("explodes")));
        assertFalse(sion.getStats().hasShield());
    }

    private static Character sionWith(SionExplodingShieldPassive passive) {
        return new Character("Sion", new Stats(270, 25, 35, 20, 35, 15),
                Type.LIGHTNING, List.of(SionExplodingShieldPassive.createMove()), List.of(passive));
    }

    private static BattleContext twoFighters(Character sion, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == foe ? List.of(foe) : List.of(sion);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == foe ? List.of(sion) : List.of(foe);
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
