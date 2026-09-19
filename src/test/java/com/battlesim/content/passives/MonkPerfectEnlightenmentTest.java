package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.MoveSelector;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class MonkPerfectEnlightenmentTest {

    private static Character monkWith(RandomProvider random) {
        Character monk = PlayableCharacters.monk().createInstance();
        monk.removePassivesOfType(MonkPerfectEnlightenmentPassive.class);
        monk.addPassive(new MonkPerfectEnlightenmentPassive(random));
        return monk;
    }

    private static Character dummy(String name, int hp, int speed) {
        return new Character(name, new Stats(hp, 40, 10, 10, 10, speed),
                Type.PHYSICAL, List.of(new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0)));
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

    private static RandomProvider neverRandom() {
        return new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                return 0.99;
            }
        };
    }

    private static RandomProvider alwaysRandom() {
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

    private static BattleContext oneVOne(Character monk, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == foe ? List.of(foe) : List.of(monk);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == foe ? List.of(monk) : List.of(foe);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }

    private static BattleContext twoVTwo(Character monk, Character ally, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                if (character == foe) {
                    return List.of(foe);
                }
                return List.of(monk, ally);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                if (character == foe) {
                    return List.of(monk, ally);
                }
                return List.of(foe);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }

    @Test
    public void monkKitIncludesPerfectEnlightenment() {
        Character monk = PlayableCharacters.monk().createInstance();
        assertTrue(monk.getMoves().stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.MOVE_NAME.equals(m.getName())));
        assertTrue(monk.hasPassive(MonkPerfectEnlightenmentPassive.class));
        assertTrue(monk.hasPassive(MonkParryPassive.class));
    }

    @Test
    public void castStartsVulnerableChannelIn1v1() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);

        List<String> log = alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                oneVOne(monk, foe));

        assertEquals(MonkPerfectEnlightenmentPassive.Phase.CHANNELING, pe.getPhase());
        assertFalse(pe.isTeamChannel());
        assertEquals(2, pe.getChannelTurnsRemaining());
        assertTrue(pe.skipsOwnAction(monk));
        assertFalse(pe.isUntargetable(monk));
        assertTrue(log.stream().anyMatch(line -> line.contains("vulnerable")));
    }

    @Test
    public void castStartsDeactivatedChannelIn2v2() {
        Character monk = monkWith(neverRandom());
        Character ally = dummy("Ally", 200, 10);
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);

        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                twoVTwo(monk, ally, foe));

        assertTrue(pe.isTeamChannel());
        assertTrue(pe.isUntargetable(monk));
        assertTrue(pe.skipsOwnAction(monk));
    }

    @Test
    public void twoSkippedTurnsBecomeEnlightenedAndSwapParryForDodge() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);

        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        assertEquals(MonkPerfectEnlightenmentPassive.Phase.CHANNELING, pe.getPhase());
        pe.onActionSkipped(monk, ctx, new ArrayList<>());

        assertEquals(MonkPerfectEnlightenmentPassive.Phase.ENLIGHTENED, pe.getPhase());
        assertFalse(monk.hasPassive(MonkParryPassive.class));
        assertTrue(monk.hasPassive(MonkPerfectDodgePassive.class));
        assertTrue(monk.hasPassive(MonkHolySplitPassive.class));
    }

    @Test
    public void stunDuringChannelBecomesAngered() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);

        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);

        Move stun = new Move("Stun", Type.PHYSICAL, 10, 100, 0, false, Status.STUN, 100);
        alwaysHits().resolveAction(foe, new ActionChoice(stun, List.of(monk)), ctx);

        assertEquals(MonkPerfectEnlightenmentPassive.Phase.ANGERED, pe.getPhase());
        assertTrue(monk.getMoves().stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.SWING_NAME.equals(m.getName())));
        assertTrue(monk.getMoves().stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.RECOVER_NAME.equals(m.getName())));
        assertFalse(monk.hasPassive(MonkParryPassive.class));
        assertFalse(monk.hasPassive(MonkHolySplitPassive.class));
    }

    @Test
    public void recoverRestoresKitAfterAngered() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);

        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onStatusReceived(monk, Status.STUN, foe, new ArrayList<>());

        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.RECOVER_NAME), List.of(monk)),
                ctx);

        assertEquals(MonkPerfectEnlightenmentPassive.Phase.IDLE, pe.getPhase());
        assertTrue(monk.hasPassive(MonkParryPassive.class));
        assertTrue(monk.hasPassive(MonkHolySplitPassive.class));
        assertFalse(monk.getMoves().stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.SWING_NAME.equals(m.getName())));
        assertTrue(monk.getMoveCooldown(MonkPerfectEnlightenmentPassive.MOVE_NAME) > 0);
    }

    @Test
    public void debuffAccuracyIsReducedDuringChannel() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                oneVOne(monk, foe));

        Move stun = new Move("Stun", Type.PHYSICAL, 10, 100, 0, false, Status.STUN, 100);
        int reduced = pe.modifyIncomingAccuracy(monk, foe, stun, 100);
        assertEquals(75, reduced);

        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        assertEquals(100, pe.modifyIncomingAccuracy(monk, foe, slash, 100));
    }

    @Test
    public void soloChannelTurnOneAmplifiesIncomingDamage() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                oneVOne(monk, foe));

        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        assertEquals(150.0, pe.modifyIncomingDamage(monk, foe, slash, 100, new ArrayList<>()), 0.01);
    }

    @Test
    public void soloChannelTurnTwoRestrictsOpponentToDebuffs() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());

        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        Move stun = new Move("Stun", Type.PHYSICAL, 10, 100, 0, false, Status.STUN, 100);
        List<Move> restricted = pe.restrictOpponentMoves(monk, foe, List.of(slash, stun), ctx);
        assertEquals(1, restricted.size());
        assertEquals("Stun", restricted.get(0).getName());
    }

    @Test
    public void enlightenedStaffBasicsDealZeroAndApplyDoubleBlessed() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        pe.onActionSkipped(monk, ctx, new ArrayList<>());

        int hpBefore = foe.getStats().getCurrentHp();
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName("Oochie"), List.of(foe)), ctx);

        assertEquals(hpBefore, foe.getStats().getCurrentHp());
        assertEquals(2, foe.getBlessedStacks());
    }

    @Test
    public void blockedHitHalvesBlessedBuildup() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        pe.onActionSkipped(monk, ctx, new ArrayList<>());

        pe.onAttackConnected(monk, foe, monk.getMoveByName("Oochie"), 0, false, true,
                new ArrayList<>(), ctx);
        assertEquals(1, foe.getBlessedStacks());
    }

    @Test
    public void perfectDodgeCountersAndAppliesBlessed() {
        MonkPerfectEnlightenmentPassive pe = new MonkPerfectEnlightenmentPassive(alwaysRandom());
        MonkPerfectDodgePassive dodge = new MonkPerfectDodgePassive(pe, alwaysRandom());
        Character monk = new Character("Roeseph", new Stats(200, 45, 35, 40, 50, 30),
                Type.HOLY, List.of(), List.of(pe, dodge));
        Character foe = dummy("Foe", 200, 10);
        int hpBefore = foe.getStats().getCurrentHp();

        assertTrue(dodge.rollDodge(monk, foe, foe.getMoves().get(0)));
        dodge.onDodged(monk, foe, foe.getMoves().get(0), oneVOne(monk, foe), new ArrayList<>());

        assertTrue(foe.getStats().getCurrentHp() < hpBefore);
        assertEquals(1, foe.getBlessedStacks());
        assertEquals(1, dodge.getDodgeStacks());
    }

    @Test
    public void divineBlessingConsumesStacksAndDealsBurst() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        foe.addBlessedStacks(MonkPerfectEnlightenmentPassive.BLESSED_CAP,
                MonkPerfectEnlightenmentPassive.BLESSED_CAP);

        int hpBefore = foe.getStats().getCurrentHp();
        List<String> log = alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);

        assertEquals(0, foe.getBlessedStacks());
        assertTrue(foe.getStats().getCurrentHp() < hpBefore);
        assertTrue(log.stream().anyMatch(line -> line.contains("Divine Blessing")));
        assertTrue(monk.getMoveCooldown(MonkPerfectEnlightenmentPassive.MOVE_NAME) > 0);
    }

    @Test
    public void enlightenmentIsHiddenWhileAlreadyEnlightenedWithoutCap() {
        Character monk = monkWith(neverRandom());
        Character foe = dummy("Foe", 200, 10);
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        BattleContext ctx = oneVOne(monk, foe);
        alwaysHits().resolveAction(monk,
                new ActionChoice(monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME), List.of(foe)),
                ctx);
        pe.onActionSkipped(monk, ctx, new ArrayList<>());
        pe.onActionSkipped(monk, ctx, new ArrayList<>());

        List<Move> available = pe.filterOwnMoves(monk, monk.getMoves(), ctx);
        assertFalse(available.stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.MOVE_NAME.equals(m.getName())));

        foe.addBlessedStacks(MonkPerfectEnlightenmentPassive.BLESSED_CAP,
                MonkPerfectEnlightenmentPassive.BLESSED_CAP);
        available = pe.filterOwnMoves(monk, monk.getMoves(), ctx);
        assertTrue(available.stream().anyMatch(m ->
                MonkPerfectEnlightenmentPassive.MOVE_NAME.equals(m.getName())));
    }

    @Test
    public void allyDeathDuringTeamChannelLosesTheFight() {
        Character monk = new Character("Roeseph", new Stats(200, 45, 35, 40, 50, 80),
                Type.HOLY,
                List.of(MonkPerfectEnlightenmentPassive.createMove()),
                List.of(new MonkPerfectEnlightenmentPassive(neverRandom())));
        Character ally = new Character("Ally", new Stats(20, 1, 1, 1, 1, 5),
                Type.PHYSICAL,
                List.of(new Move("Poke", Type.PHYSICAL, 1, 100, 0, false, Status.NONE, 0)));
        Character foe = new Character("Foe", new Stats(200, 200, 1, 1, 1, 40),
                Type.PHYSICAL,
                List.of(new Move("Smash", Type.PHYSICAL, 200, 100, 0, false, Status.NONE, 0)));

        MoveSelector scripted = (actor, available, enemies, allies) -> {
            if (actor == monk) {
                return new ActionChoice(available.get(0), List.of(foe));
            }
            Character target = enemies.contains(ally) && !ally.isFainted() ? ally : enemies.get(0);
            return new ActionChoice(available.get(0), List.of(target));
        };

        BattleResult result = Battle.create(
                new Team(List.of(monk, ally)), new Team(List.of(foe)),
                scripted, scripted, alwaysRandom(), 20, false).run();

        assertEquals(BattleResult.Winner.TEAM_B, result.getWinner());
        assertTrue(result.getLog().stream().anyMatch(line -> line.contains("fails as their ally falls")));
    }

    @Test
    public void channelCompletesAcrossBattleTurns() {
        Character monk = new Character("Roeseph", new Stats(200, 45, 35, 40, 50, 50),
                Type.HOLY,
                List.of(MonkPerfectEnlightenmentPassive.createMove(),
                        new Move("Oochie", Type.HOLY, 50, 100, 0, false, Status.NONE, 0)),
                List.of(new MonkHolySplitPassive(), new MonkParryPassive(),
                        new MonkPerfectEnlightenmentPassive(neverRandom())));
        Character foe = dummy("Foe", 400, 1);

        MoveSelector scripted = (actor, available, enemies, allies) ->
                new ActionChoice(available.get(0), List.of(enemies.get(0)));

        Battle.create(new Team(List.of(monk)), new Team(List.of(foe)),
                scripted, scripted, alwaysRandom(), 6, false).run();

        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        assertEquals(MonkPerfectEnlightenmentPassive.Phase.ENLIGHTENED, pe.getPhase());
        assertTrue(monk.hasPassive(MonkPerfectDodgePassive.class));
    }
}
