package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Team;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SionRevivePassiveTest {

    @Test
    public void sionTemplateHasRevivePassive() {
        Character sion = PlayableCharacters.sion().createInstance();
        assertTrue(sion.hasPassive(SionRevivePassive.class));
    }

    @Test
    public void onFaintAddsUnboundUndeadAllyOnce() {
        SionRevivePassive passive = new SionRevivePassive();
        Character sion = new Character("Sion", new Stats(10, 10, 10, 10, 10, 10),
                Type.LIGHTNING, List.of(), List.of(passive));
        sion.getStats().applyDamage(10);

        List<Character> added = new ArrayList<>();
        BattleContext context = new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return List.of(sion);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return List.of();
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
                added.add(summon);
            }
        };

        List<String> log = new ArrayList<>();
        passive.onFaint(sion, null, null, context, log);
        assertEquals(1, added.size());
        Character undead = added.get(0);
        assertEquals(SionRevivePassive.UNDEAD_NAME, undead.getName());
        assertEquals(Type.UNDEAD, undead.getAffinity());
        assertFalse(undead.isSummon());
        assertFalse(undead.hasPassive(SionRevivePassive.class));
        assertFalse(undead.isFainted());

        passive.onFaint(sion, null, null, context, log);
        assertEquals(1, added.size());
    }

    @Test
    public void faintedSionSpawnsUndeadThatDoesNotFade() {
        Move poke = new Move("Poke", Type.PHYSICAL, 1, 100, 0, false, Status.NONE, 0);
        Character sion = new Character("Sion", new Stats(10, 1, 100, 1, 100, 10),
                Type.LIGHTNING, List.of(poke), List.of(new SionRevivePassive()));
        Character foe = new Character("Foe", new Stats(500, 200, 1, 1, 1, 80),
                Type.PHYSICAL, List.of(new Move("Smash", Type.PHYSICAL, 80, 100, 0, false, Status.NONE, 0)));

        RandomProvider alwaysHits = alwaysHits();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(alwaysHits);
        BattleResult result = Battle.create(
                new Team(List.of(sion)), new Team(List.of(foe)),
                ai, ai, alwaysHits, 50, false).run();

        assertTrue(result.getLog().stream().anyMatch(line ->
                line.contains(SionRevivePassive.UNDEAD_NAME) && line.contains("rises")));
        assertTrue(result.getLog().stream().noneMatch(line -> line.contains("fades away")));
        long undeadCount = result.getFighters().stream()
                .filter(fighter -> SionRevivePassive.UNDEAD_NAME.equals(fighter.getName()))
                .count();
        assertEquals(1, undeadCount);
    }

    @Test
    public void burnFaintAlsoSpawnsUndeadSion() {
        Move poke = new Move("Poke", Type.PHYSICAL, 1, 100, 0, false, Status.NONE, 0);
        Character sion = new Character("Sion", new Stats(10, 1, 100, 1, 100, 100),
                Type.LIGHTNING, List.of(poke), List.of(new SionRevivePassive()));
        sion.getStats().applyDamage(9);
        sion.setStatus(Status.BURN);
        Character foe = new Character("Foe", new Stats(500, 1, 100, 1, 100, 1),
                Type.PHYSICAL, List.of(poke));

        RandomProvider alwaysHits = alwaysHits();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(alwaysHits);
        BattleResult result = Battle.create(
                new Team(List.of(sion)), new Team(List.of(foe)),
                ai, ai, alwaysHits, 20, false).run();

        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("damage from burn")));
        assertTrue(result.getLog().stream().anyMatch(line ->
                line.contains(SionRevivePassive.UNDEAD_NAME) && line.contains("rises")));
        assertTrue(result.getFighters().stream().anyMatch(fighter ->
                SionRevivePassive.UNDEAD_NAME.equals(fighter.getName()) && "A".equals(fighter.getTeamId())));
    }

    private static RandomProvider alwaysHits() {
        return new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
    }
}
