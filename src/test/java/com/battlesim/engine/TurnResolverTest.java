package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class TurnResolverTest {

    private Character character(String name, int maxHp, int attack, int magicAttack) {
        Stats stats = new Stats(maxHp, attack, 10, magicAttack, 10, 10);
        return new Character(name, stats, Type.PHYSICAL, List.of());
    }

    private TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    @Test
    public void healMoveRestoresAllyHealthInsteadOfDealingDamage() {
        Character healer = character("Healer", 175, 10, 57);
        Character ally = character("Ally", 200, 10, 10);
        ally.getStats().applyDamage(80);

        Move heal = new Move("Heal", Type.HOLY, 0, 100, 0, true, Status.HEAL, 100);
        List<String> log = alwaysHits().resolveAction(healer, new ActionChoice(heal, List.of(ally)));

        assertEquals(170, ally.getStats().getCurrentHp());
        assertEquals(Status.NONE, ally.getStatus());
        assertTrue(log.stream().anyMatch(line -> line.contains("recovers 50 HP")));
    }

    @Test
    public void leechMoveHealsAttackerForFifteenPercentOfDamageDealt() {
        Character caster = character("Caster", 200, 10, 50);
        caster.getStats().applyDamage(80);
        Character enemy = character("Enemy", 200, 10, 10);
        Move leech = new Move("Leech", Type.HOLY, 40, 100, 0, true, Status.LEECH, 100);

        List<String> log = alwaysHits().resolveAction(caster, new ActionChoice(leech, List.of(enemy)));

        assertTrue(caster.getStats().getCurrentHp() > 120);
        assertEquals(Status.NONE, enemy.getStatus());
        assertTrue(log.stream().anyMatch(line -> line.contains("leeches")));
    }

    @Test
    public void defenderEvasionCanTurnAHitIntoADodge() {
        Character attacker = character("Attacker", 200, 50, 0);
        Passive highEvasion = new Passive() {
            @Override
            public int modifyIncomingAccuracy(Character self, Character source, Move move, int accuracy) {
                return accuracy - 100;
            }
        };
        Character defender = new Character("Defender",
                new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(), List.of(highEvasion));
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(slash, List.of(defender)));

        assertEquals(200, defender.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("dodges the attack")));
    }
}
