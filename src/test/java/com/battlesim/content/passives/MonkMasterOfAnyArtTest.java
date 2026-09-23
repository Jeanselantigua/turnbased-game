package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.MoveKit;
import com.battlesim.model.Passive;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class MonkMasterOfAnyArtTest {

    private static TurnResolver resolver(RandomProvider random) {
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    private static RandomProvider alwaysConnects() {
        return new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }
        };
    }

    /** Accuracy and status rolls that fail 70%/60% originals but succeed at 100. */
    private static RandomProvider harshRolls() {
        return new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                if (min == 1 && max == 100) {
                    return 80;
                }
                return min;
            }
        };
    }

    private static Character bulky(Character fighter) {
        fighter.getStats().increaseMaxHp(8000);
        return fighter;
    }

    private static Move masterMove() {
        return MonkMasterOfAnyArtPassive.createMove();
    }

    @Test
    public void copiesRogueUltOnceDetonatesWoundsAndCoolsDown() {
        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();
        Character rogue = bulky(PlayableCharacters.rogue().createFullyLearnedInstance());
        rogue.addWoundStacks(3, 2);
        MonkMasterOfAnyArtPassive art = monk.getPassive(MonkMasterOfAnyArtPassive.class);

        List<String> log = resolver(alwaysConnects()).resolveAction(monk,
                new ActionChoice(masterMove(), List.of(rogue)));

        assertTrue(log.stream().anyMatch(line -> line.contains("masters Assassinate")));
        assertTrue(log.stream().anyMatch(line -> line.contains("uses Assassinate")));
        assertTrue(log.stream().anyMatch(line -> line.contains("wound stack")));
        assertEquals(0, rogue.getWoundStacks());
        assertEquals(0, art.getUsesRemaining());
        assertNull(art.getCopiedArt());
        assertEquals(Growth.ULT_COOLDOWN_TURNS,
                monk.getMoveCooldown(MonkMasterOfAnyArtPassive.MOVE_NAME));
    }

    @Test
    public void copiesCavemanUltNeverMissesAndAlwaysStuns() {
        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();
        Character caveman = bulky(PlayableCharacters.caveman().createFullyLearnedInstance());
        Passive alwaysDodge = new Passive() {
            @Override
            public boolean rollDodge(Character self, Character attacker, Move move) {
                return true;
            }
        };
        caveman.addPassive(alwaysDodge);

        List<String> log = resolver(harshRolls()).resolveAction(monk,
                new ActionChoice(masterMove(), List.of(caveman)));

        assertTrue(log.stream().anyMatch(line -> line.contains("masters Meteor Club")));
        assertTrue(log.stream().noneMatch(line -> line.contains("Missed") || line.contains("dodges")));
        assertTrue(caveman.hasStatus(Status.STUN));
        assertEquals(Growth.ULT_COOLDOWN_TURNS,
                monk.getMoveCooldown(MonkMasterOfAnyArtPassive.MOVE_NAME));
    }

    @Test
    public void dungeonEnemyWithoutUltCopiesBestMoveThreeTimes() {
        Move poke = new Move("Poke", Type.PHYSICAL, 20, 90, 0, false, Status.NONE, 0);
        Move smash = new Move("Smash", Type.PHYSICAL, 80, 80, 0, false, Status.NONE, 0);
        Character goblin = new Character("Goblin", new Stats(9999, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(poke, smash), List.of(), List.of(),
                MoveKit.alwaysKnown(poke, smash));
        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();
        MonkMasterOfAnyArtPassive art = monk.getPassive(MonkMasterOfAnyArtPassive.class);
        TurnResolver hits = resolver(alwaysConnects());
        ActionChoice choice = new ActionChoice(masterMove(), List.of(goblin));

        List<String> first = hits.resolveAction(monk, choice);
        assertTrue(first.stream().anyMatch(line -> line.contains("masters Smash (3 uses)")));
        assertEquals("Smash", art.getCopiedArt().getName());
        assertEquals(2, art.getUsesRemaining());
        assertEquals(0, monk.getMoveCooldown(MonkMasterOfAnyArtPassive.MOVE_NAME));

        hits.resolveAction(monk, choice);
        assertEquals(1, art.getUsesRemaining());
        assertEquals(0, monk.getMoveCooldown(MonkMasterOfAnyArtPassive.MOVE_NAME));

        hits.resolveAction(monk, choice);
        assertEquals(0, art.getUsesRemaining());
        assertNull(art.getCopiedArt());
        assertEquals(Growth.ULT_COOLDOWN_TURNS,
                monk.getMoveCooldown(MonkMasterOfAnyArtPassive.MOVE_NAME));
    }

    @Test
    public void prefersKnownUltOverAStrongerBasic() {
        Move basic = new Move("Swipe", Type.PHYSICAL, 200, 100, 0, false, Status.NONE, 0);
        Move ult = new Move("Finisher", Type.PHYSICAL, 40, 90, 0, false, Status.STUN, 40,
                Growth.ULT_COOLDOWN_TURNS);
        Character foe = new Character("Elite", new Stats(500, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(basic, ult), List.of(), List.of(),
                MoveKit.ladder(basic, basic, basic, ult));

        assertEquals("Finisher", MonkMasterOfAnyArtPassive.bestMoveToCopy(foe).getName());
        assertEquals("Smash", MonkMasterOfAnyArtPassive.bestMoveToCopy(
                new Character("Goblin", new Stats(50, 10, 10, 10, 10, 10), Type.PHYSICAL,
                        List.of(new Move("Poke", Type.PHYSICAL, 20, 90, 0, false, Status.NONE, 0),
                                new Move("Smash", Type.PHYSICAL, 80, 80, 0, false, Status.NONE, 0)))).getName());
    }

    @Test
    public void copiedStatusCannotMissEvenWhenTheOriginalChanceWouldFail() {
        Move club = new Move("Bonk", Type.PHYSICAL, 10, 100, 0, false, Status.STUN, 40);
        Character foe = new Character("Dummy", new Stats(500, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(club));
        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();

        resolver(harshRolls()).resolveAction(monk, new ActionChoice(masterMove(), List.of(foe)));
        assertTrue(foe.hasStatus(Status.STUN));
    }

    @Test
    public void copiedMoveBecomesHolyAndGainsStab() {
        Move smash = new Move("Smash", Type.PHYSICAL, 80, 100, 0, false, Status.NONE, 0);
        Move copy = MonkMasterOfAnyArtPassive.perfectCopy(smash);
        assertEquals(Type.HOLY, copy.getType());
        assertEquals(smash.getPower(), copy.getPower());

        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();
        Character dummy = new Character("Dummy", new Stats(9999, 1, 10, 1, 10, 10),
                Type.PHYSICAL, List.of(smash));
        DamageCalculator calc = new DamageCalculator(new TypeChart(), alwaysConnects());
        int physical = calc.calculateDamage(monk, dummy, smash);
        int holy = calc.calculateDamage(monk, dummy, copy);
        assertTrue(holy > physical);
        assertEquals(1.5, holy / (double) physical, 0.05);
    }
}
