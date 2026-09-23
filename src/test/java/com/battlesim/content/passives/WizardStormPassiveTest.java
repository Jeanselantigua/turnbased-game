package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class WizardStormPassiveTest {

    @Test
    public void chainLightningJumpsAlongAdjacentEnemies() {
        RandomProvider always = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
        WizardStormPassive storm = new WizardStormPassive(always);
        Move chain = new Move(WizardStormPassive.CHAIN_NAME, Type.LIGHTNING, 35, 100, 0, true,
                Status.PARALYSIS, 0);
        Character wizard = new Character("WEWE Head", new Stats(200, 10, 10, 60, 10, 40),
                Type.LIGHTNING, List.of(chain), List.of(storm));
        Character left = dummy("Left");
        Character mid = dummy("Mid");
        Character right = dummy("Right");
        BattleContext context = field(wizard, List.of(left, mid, right));

        alwaysHits(always).resolveAction(wizard, new ActionChoice(chain, List.of(left)), context);

        assertTrue(left.getStats().getCurrentHp() < 200);
        assertTrue(mid.getStats().getCurrentHp() < 200);
        assertTrue(right.getStats().getCurrentHp() < 200);
    }

    @Test
    public void thunderGodsWrathHitsEveryEnemy() {
        Move wrath = new Move(WizardStormPassive.WRATH_NAME, Type.LIGHTNING, 75, 100, 0, true,
                Status.PARALYSIS, 0, 0, 1, 1, true);
        Character wizard = new Character("WEWE Head", new Stats(200, 10, 10, 60, 10, 40),
                Type.LIGHTNING, List.of(wrath), List.of(new WizardStormPassive()));
        Character a = dummy("A");
        Character b = dummy("B");
        Character c = dummy("C");
        BattleContext context = field(wizard, List.of(a, b, c));

        alwaysHits(new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        }).resolveAction(wizard, new ActionChoice(wrath, List.of(b)), context);

        assertTrue(a.getStats().getCurrentHp() < 200);
        assertTrue(b.getStats().getCurrentHp() < 200);
        assertTrue(c.getStats().getCurrentHp() < 200);
        assertTrue(com.battlesim.content.PlayableCharacters.wizard().getKit().getUlt().hitsAllEnemies());
    }

    private static Character dummy(String name) {
        return new Character(name, new Stats(200, 10, 10, 10, 10, 10), Type.PHYSICAL, List.of());
    }

    private static BattleContext field(Character wizard, List<Character> foes) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == wizard ? List.of(wizard) : foes;
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == wizard ? foes : List.of(wizard);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }

    private static TurnResolver alwaysHits(RandomProvider random) {
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }
}
