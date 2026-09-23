package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.engine.StatusEffectResolver;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Team;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class MechanizedChampionTest {

    @Test
    public void sheetUsesArenaStatsAndTheNewKit() {
        Character arena = PlayableCharacters.arenaRoster().get(0).createFullyLearnedInstance();
        assertEquals("Mechanized Champion", arena.getName());
        assertEquals(200, arena.getStats().getMaxHp());
        assertEquals(57, arena.getStats().getAttack());
        assertEquals(37, arena.getStats().getDefense());
        assertEquals(0, arena.getStats().getMagicAttack());
        assertEquals(-20, arena.getStats().getMagicDefense());
        assertEquals(26, arena.getStats().getSpeed());
        assertEquals(ChampionsStrengthPassive.UPLIFTING_SLASH, arena.getMoves().get(0).getName());
        assertEquals(PridefulTauntPassive.MOVE_NAME, arena.getMoves().get(1).getName());
        assertEquals(ChampionsStrengthPassive.VISCERAL_WOUND, arena.getMoves().get(2).getName());
        assertEquals(WillOfHumanityPassive.MOVE_NAME, arena.getKit().getUlt().getName());
        assertTrue(arena.hasPassive(ChampionsStrengthPassive.class));
        assertTrue(arena.hasPassive(WellOiledRagePassive.class));
        assertTrue(arena.hasPassive(PridefulTauntPassive.class));
        assertTrue(arena.hasPassive(WillOfHumanityPassive.class));

        Character dungeon = PlayableCharacters.knight().createInstance();
        assertEquals(-10, dungeon.getStats().getMagicDefense());
        assertEquals(1, dungeon.getMoves().size());
        assertFalse(dungeon.knowsMove(WillOfHumanityPassive.MOVE_NAME));
    }

    @Test
    public void upliftingSlashDealsFlatDamageAndBuildsCrit() {
        Character champion = PlayableCharacters.knight().createFullyLearnedInstance();
        Character wall = new Character("Wall", new Stats(100, 1, 9999, 1, 9999, 1),
                Type.PHYSICAL, List.of());
        Move slash = champion.getMoveByName(ChampionsStrengthPassive.UPLIFTING_SLASH);

        alwaysHits().resolveAction(champion, new ActionChoice(slash, List.of(wall)));

        assertEquals(100 - ChampionsStrengthPassive.UPLIFTING_DAMAGE, wall.getStats().getCurrentHp());
        assertEquals(ChampionsStrengthPassive.CRIT_PER_HIT,
                champion.getPassive(ChampionsStrengthPassive.class).getBonusCrit());
        assertEquals(ChampionsStrengthPassive.CRIT_PER_HIT, champion.getCritRate(slash));
    }

    @Test
    public void critResetsTheBonus() {
        ChampionsStrengthPassive strength = new ChampionsStrengthPassive();
        Character champion = new Character("Mechanized Champion", new Stats(200, 40, 20, 0, 10, 20),
                Type.PHYSICAL, List.of(ChampionsStrengthPassive.createUpliftingSlash()),
                List.of(strength));
        Character dummy = new Character("Dummy", new Stats(500, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        Move slash = champion.getMoves().get(0);
        TurnResolver resolver = neverCrits();

        resolver.resolveAction(champion, new ActionChoice(slash, List.of(dummy)));
        resolver.resolveAction(champion, new ActionChoice(slash, List.of(dummy)));
        assertEquals(ChampionsStrengthPassive.CRIT_PER_HIT * 2, strength.getBonusCrit());

        alwaysCrits().resolveAction(champion, new ActionChoice(slash, List.of(dummy)));
        assertEquals(0, strength.getBonusCrit());
        assertEquals(500 - 35 - 35 - 70, dummy.getStats().getCurrentHp());
    }

    @Test
    public void killHealsThePartyForAQuarterOfTheVictimMaxHp() {
        ChampionsStrengthPassive strength = new ChampionsStrengthPassive();
        Character champion = new Character("Mechanized Champion", new Stats(200, 40, 20, 0, 10, 20),
                Type.PHYSICAL, List.of(ChampionsStrengthPassive.createVisceralWound()),
                List.of(strength));
        Character ally = new Character("Ally", new Stats(100, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        ally.getStats().applyDamage(50);
        Character foe = new Character("Foe", new Stats(80, 10, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        foe.getStats().applyDamage(79);

        alwaysHits().resolveAction(champion,
                new ActionChoice(champion.getMoves().get(0), List.of(foe)),
                party(champion, ally, foe));

        assertTrue(foe.isFainted());
        assertEquals(70, ally.getStats().getCurrentHp());
    }

    @Test
    public void rageBoostsEnemyHitsAndClipsAlliesWithoutStatus() {
        Character champion = PlayableCharacters.knight().createFullyLearnedInstance();
        champion.getStats().applyDamage(champion.getStats().getMaxHp() / 2 + 1);
        Character ally = new Character("Ally", new Stats(100, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Character foe = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Move slash = champion.getMoveByName(ChampionsStrengthPassive.UPLIFTING_SLASH);

        List<String> log = neverCrits().resolveAction(champion,
                new ActionChoice(slash, List.of(foe)),
                party(champion, ally, foe));

        int expectedEnemy = (int) Math.round(
                ChampionsStrengthPassive.UPLIFTING_DAMAGE * (1.0 + WellOiledRagePassive.DAMAGE_BONUS));
        assertEquals(200 - expectedEnemy, foe.getStats().getCurrentHp());
        assertEquals(100 - ChampionsStrengthPassive.UPLIFTING_DAMAGE, ally.getStats().getCurrentHp());
        assertTrue(foe.hasStatus(Status.PARALYSIS));
        assertFalse(ally.hasStatus(Status.PARALYSIS));
        assertTrue(log.stream().anyMatch(line -> line.contains("blind fury")));
        assertTrue(log.stream().anyMatch(line -> line.contains("crossfire")));
    }

    @Test
    public void visceralWoundAppliesNormalBleed() {
        Character champion = PlayableCharacters.knight().createFullyLearnedInstance();
        Character foe = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Move wound = champion.getMoveByName(ChampionsStrengthPassive.VISCERAL_WOUND);

        alwaysHits().resolveAction(champion, new ActionChoice(wound, List.of(foe)));
        assertTrue(foe.hasStatus(Status.BLEED));
        assertEquals(champion.getStats().getAttack(), foe.getStatusMagnitude(Status.BLEED));
        assertEquals(200 - ChampionsStrengthPassive.VISCERAL_DAMAGE, foe.getStats().getCurrentHp());

        int bleedTick = (int) Math.round(champion.getStats().getAttack() * 0.40);
        new StatusEffectResolver().applyStartOfTurnEffects(foe, new java.util.ArrayList<>());
        assertEquals(200 - ChampionsStrengthPassive.VISCERAL_DAMAGE - bleedTick,
                foe.getStats().getCurrentHp());
    }

    @Test
    public void tauntPullsAttacksAndSoftensAKillingBlow() {
        Character champion = new Character("Mechanized Champion", new Stats(200, 30, 100, 0, 10, 80),
                Type.PHYSICAL, List.of(PridefulTauntPassive.createMove(),
                        ChampionsStrengthPassive.createUpliftingSlash()),
                List.of(new PridefulTauntPassive()));
        Character ally = new Character("Ally", new Stats(30, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        Character foe = new Character("Foe", new Stats(200, 40, 10, 10, 10, 40),
                Type.PHYSICAL, List.of(new Move("Poke", Type.PHYSICAL, 30, 100, 0, false, Status.NONE, 0)));

        BattleResult result = Battle.create(
                new Team(List.of(champion, ally)), new Team(List.of(foe)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new RandomProvider(1L), 2, false).run();

        assertEquals(ally.getStats().getMaxHp(), ally.getStats().getCurrentHp());
        assertTrue(champion.getStats().getCurrentHp() < champion.getStats().getMaxHp());
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("All eyes")));

        PridefulTauntPassive taunt = champion.getPassive(PridefulTauntPassive.class);
        double lethal = taunt.modifyIncomingDamage(champion, foe, foe.getMoves().get(0), 500, new java.util.ArrayList<>());
        double expected = 500 / (1.0 + PridefulTauntPassive.DEFENSE_BONUS) * PridefulTauntPassive.LETHAL_REMAINING;
        assertEquals(expected, lethal, 0.001);
    }

    @Test
    public void willOfHumanityRevivesOnceAndSpendsTheBlade() {
        Character champion = PlayableCharacters.knight().createFullyLearnedInstance();
        WillOfHumanityPassive will = champion.getPassive(WillOfHumanityPassive.class);
        Character foe = new Character("Foe", new Stats(400, 500, 20, 1, 1, 100),
                Type.PHYSICAL, List.of(new Move("Smash", Type.PHYSICAL, 200, 100, 0, false, Status.NONE, 0)));

        BattleResult result = Battle.create(
                new Team(List.of(champion)), new Team(List.of(foe)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new RandomProvider(1L), 1, false).run();

        assertFalse(champion.isFainted());
        assertEquals(champion.getStats().getMaxHp(), champion.getStats().getCurrentHp());
        assertTrue(will.isBladeSpent());
        assertEquals(WillOfHumanityPassive.GUARD_TURNS, will.getGuardTurns());
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("Will of Humanity")));

        assertEquals(100 * WillOfHumanityPassive.INCOMING_MULTIPLIER,
                will.modifyIncomingDamage(champion, foe, null, 100, new java.util.ArrayList<>()), 0.001);
        assertEquals(100 * WillOfHumanityPassive.OUTGOING_MULTIPLIER,
                will.modifyOutgoingDamage(champion, foe, null, 100, false, new java.util.ArrayList<>()), 0.001);

        champion.getStats().applyDamage(champion.getStats().getMaxHp());
        will.onFaint(champion, foe, null, null, new java.util.ArrayList<>());
        assertTrue(champion.isFainted());

        Character ready = PlayableCharacters.knight().createFullyLearnedInstance();
        Battle battle = Battle.create(
                new Team(List.of(ready)), new Team(List.of(foe)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new SimpleAiMoveSelector(new RandomProvider(1L)),
                new RandomProvider(1L), 1, false);
        List<Move> moves = battle.availableMovesFor(ready);
        assertFalse(moves.stream().anyMatch(move -> WillOfHumanityPassive.MOVE_NAME.equals(move.getName())));
        assertEquals(3, moves.size());
    }

    @Test
    public void unlearnedUltDoesNotRevive() {
        WillOfHumanityPassive will = new WillOfHumanityPassive();
        Character champion = PlayableCharacters.knight().createInstance();
        champion.getPassives().clear();
        champion.getPassives().add(will);
        champion.getStats().applyDamage(champion.getStats().getMaxHp());

        will.onFaint(champion, null, null, null, new java.util.ArrayList<>());

        assertTrue(champion.isFainted());
        assertFalse(will.isBladeSpent());
    }

    private static TurnResolver alwaysHits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static TurnResolver neverCrits() {
        RandomProvider random = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                return 0.99;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static TurnResolver alwaysCrits() {
        return alwaysHits();
    }

    private static BattleContext party(Character champion, Character ally, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                if (character == foe) {
                    return List.of(foe);
                }
                return List.of(champion, ally);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                if (character == foe) {
                    return List.of(champion, ally);
                }
                return List.of(foe);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }
}
