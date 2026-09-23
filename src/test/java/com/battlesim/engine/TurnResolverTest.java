package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.passives.DualSwordsmanStatusRerollPassive;
import com.battlesim.content.passives.ExtraActionsPassive;
import com.battlesim.model.BattleContext;
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
    public void shieldMoveGrantsShieldToAllyInsteadOfDealingDamage() {
        Character caster = character("Caster", 200, 10, 40);
        Character ally = character("Ally", 200, 10, 10);

        Move shield = new Move("Barrier", Type.HOLY, 80, 100, 0, true, Status.SHIELD, 100);
        List<String> log = alwaysHits().resolveAction(caster, new ActionChoice(shield, List.of(ally)));

        assertEquals(80, ally.getStats().getShieldHp());
        assertEquals(200, ally.getStats().getCurrentHp());
        assertEquals(Status.NONE, ally.getStatus());
        assertTrue(log.stream().anyMatch(line -> line.contains("80 HP shield")));
    }

    @Test
    public void shieldMoveCanTargetSelf() {
        Character caster = character("Caster", 200, 10, 40);
        Move shield = new Move("Barrier", Type.HOLY, 100, 100, 0, true, Status.SHIELD, 100);

        alwaysHits().resolveAction(caster, new ActionChoice(shield, List.of(caster)));

        assertEquals(100, caster.getStats().getShieldHp());
        assertEquals(200, caster.getStats().getCurrentHp());
    }

    @Test
    public void selfShieldMoveAlwaysShieldsTheCaster() {
        Character caster = character("Caster", 200, 10, 40);
        Character ally = character("Ally", 200, 10, 10);
        Move roar = new Move("Roar", Type.UNDEAD, 100, 100, 0, true, Status.SELF_SHIELD, 100);

        List<String> log = alwaysHits().resolveAction(caster, new ActionChoice(roar, List.of(ally)));

        assertEquals(100, caster.getStats().getShieldHp());
        assertEquals(0, ally.getStats().getShieldHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("Caster") && line.contains("100 HP shield")));
    }

    @Test
    public void combatDamageHitsShieldBeforeHp() {
        Character attacker = character("Attacker", 200, 50, 0);
        Character defender = character("Defender", 200, 10, 10);
        defender.getStats().grantShield(40);
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(slash, List.of(defender)));

        assertTrue(defender.getStats().getCurrentHp() < 200);
        assertTrue(defender.getStats().getShieldHp() < 40);
        assertTrue(log.stream().anyMatch(line -> line.contains("shield absorbs")));
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
    public void slowMoveAppliesSlowStatus() {
        Character caster = character("Caster", 200, 50, 50);
        Character enemy = character("Enemy", 200, 10, 10);
        Move slow = new Move("Slow", Type.PHYSICAL, 20, 100, 0, true, Status.SLOW, 100);

        List<String> log = alwaysHits().resolveAction(caster, new ActionChoice(slow, List.of(enemy)));

        assertEquals(Status.SLOW, enemy.getStatus());
        assertEquals(5, enemy.getEffectiveSpeed());
        assertTrue(log.stream().anyMatch(line -> line.contains("SLOW")));
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

    @Test
    public void bleedMoveSnapshotsAttackerAttackAsMagnitude() {
        Character attacker = character("Knight", 200, 80, 0);
        Character enemy = character("Enemy", 200, 10, 10);
        Move rend = new Move("Rend", Type.PHYSICAL, 20, 100, 0, false, Status.BLEED, 100);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(rend, List.of(enemy)));

        assertEquals(Status.BLEED, enemy.getStatus());
        assertEquals(80, enemy.getStatusMagnitude());
        assertTrue(log.stream().anyMatch(line -> line.contains("BLEED")));
    }

    @Test
    public void dualSwordsmanBleedSnapshotsOneAndAHalfAttack() {
        DualSwordsmanStatusRerollPassive statusPassive = new DualSwordsmanStatusRerollPassive();
        Character attacker = new Character("Dual Swordsman",
                new Stats(180, 50, 50, 60, 30, 30),
                Type.LIGHTNING, List.of(), List.of(statusPassive));
        Character enemy = character("Enemy", 200, 10, 10);
        Move rend = new Move("Diagonal Strike", Type.PHYSICAL, 20, 100, 0, false, Status.BLEED, 100);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(rend, List.of(enemy)));

        assertEquals(Status.BLEED, enemy.getStatus());
        assertEquals(75, enemy.getStatusMagnitude());
        assertTrue(log.stream().anyMatch(line -> line.contains("makes bleed deeper")));
    }

    @Test
    public void dualSwordsmanDoesNotDoubleParalysis() {
        DualSwordsmanStatusRerollPassive statusPassive = new DualSwordsmanStatusRerollPassive();
        Character attacker = new Character("Dual Swordsman",
                new Stats(180, 50, 50, 60, 30, 30),
                Type.LIGHTNING, List.of(), List.of(statusPassive));
        Character enemy = character("Enemy", 400, 10, 10);
        Move shock = new Move("Lightning slash", Type.LIGHTNING, 20, 100, 0, false, Status.PARALYSIS, 100);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(shock, List.of(enemy)));

        assertEquals(Status.PARALYSIS, enemy.getStatus());
        assertEquals(0, enemy.getStatusMagnitude());
        assertFalse(log.stream().anyMatch(line -> line.contains("seize for 2 turns")));
    }

    @Test
    public void defaultParalysisSkipDoesNotQueueASecondTurn() {
        Character paralyzed = character("Victim", 200, 10, 10);
        paralyzed.setStatus(Status.PARALYSIS);
        Move slash = new Move("Slash", Type.PHYSICAL, 20, 100, 0, false, Status.NONE, 0);
        Character dummy = character("Dummy", 200, 10, 10);

        RandomProvider paralysisRandom = new RandomProvider() {
            private int doubles = 0;

            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                doubles++;
                return doubles == 1 ? 0.0 : 0.99;
            }
        };
        TurnResolver resolver = new TurnResolver(new DamageCalculator(new TypeChart(), paralysisRandom),
                paralysisRandom);

        List<String> firstSkip = resolver.resolveAction(paralyzed, new ActionChoice(slash, List.of(dummy)));
        assertTrue(firstSkip.stream().anyMatch(line -> line.contains("paralyzed")));
        assertEquals(0, paralyzed.getQueuedSkipTurns());

        List<String> acts = resolver.resolveAction(paralyzed, new ActionChoice(slash, List.of(dummy)));
        assertTrue(acts.stream().anyMatch(line -> line.contains("uses Slash")));
    }

    @Test
    public void repeatActionResolvesTheSameMoveASecondTime() {
        Passive alwaysRepeat = new Passive() {
            @Override
            public boolean shouldRepeatAction(Character self, Move move, List<Character> targets,
                                               BattleContext context, List<String> log) {
                log.add(self.getName() + " strikes again!");
                return true;
            }
        };
        Character attacker = new Character("Dualist",
                new Stats(200, 50, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(), List.of(alwaysRepeat));
        Character defender = character("Defender", 500, 10, 10);
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(slash, List.of(defender)));

        long hits = log.stream().filter(line -> line.contains("took")).count();
        assertEquals(2, hits);
        assertTrue(log.stream().anyMatch(line -> line.contains("strikes again")));
        assertTrue(defender.getStats().getCurrentHp() < 500);
    }

    @Test
    public void repeatActionDoesNotChainAThirdHit() {
        Passive alwaysRepeat = new Passive() {
            @Override
            public boolean shouldRepeatAction(Character self, Move move, List<Character> targets,
                                               BattleContext context, List<String> log) {
                return true;
            }
        };
        Character attacker = new Character("Dualist",
                new Stats(200, 50, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(), List.of(alwaysRepeat));
        Character defender = character("Defender", 500, 10, 10);
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        List<String> log = alwaysHits().resolveAction(attacker, new ActionChoice(slash, List.of(defender)));

        long hits = log.stream().filter(line -> line.contains("took")).count();
        assertEquals(2, hits);
    }

    @Test
    public void failedStatusCanBeRerolledAndStillApply() {
        Passive alwaysReroll = new Passive() {
            @Override
            public boolean shouldRerollFailedStatus(Character self, Character target, Move move,
                                                     List<String> log) {
                return true;
            }
        };
        Character attacker = new Character("Dualist",
                new Stats(200, 50, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(), List.of(alwaysReroll));
        Character enemy = character("Enemy", 200, 10, 10);
        Move sting = new Move("Sting", Type.PHYSICAL, 20, 100, 0, false, Status.BLEED, 30);

        List<String> log = sequencedInts(1, 85, 100, 1)
                .resolveAction(attacker, new ActionChoice(sting, List.of(enemy)));

        assertEquals(Status.BLEED, enemy.getStatus());
        assertTrue(log.stream().anyMatch(line -> line.contains("BLEED")));
    }

    @Test
    public void statusRerollCanFailAgain() {
        Passive alwaysReroll = new Passive() {
            @Override
            public boolean shouldRerollFailedStatus(Character self, Character target, Move move,
                                                     List<String> log) {
                return true;
            }
        };
        Character attacker = new Character("Dualist",
                new Stats(200, 50, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(), List.of(alwaysReroll));
        Character enemy = character("Enemy", 200, 10, 10);
        Move sting = new Move("Sting", Type.PHYSICAL, 20, 100, 0, false, Status.BLEED, 30);

        sequencedInts(1, 85, 100, 100)
                .resolveAction(attacker, new ActionChoice(sting, List.of(enemy)));

        assertEquals(Status.NONE, enemy.getStatus());
    }

    private TurnResolver sequencedInts(int... values) {
        RandomProvider random = new RandomProvider() {
            private int i = 0;

            @Override
            public int nextInt(int min, int max) {
                if (i < values.length) {
                    return values[i++];
                }
                return min;
            }
        };
        return new TurnResolver(new DamageCalculator(new TypeChart(), random), random);
    }

    @Test
    public void onDamageTakenCanAddAPassiveWithoutCrashing() {
        Character attacker = character("Attacker", 200, 80, 0);
        Passive phasing = new Passive() {
            @Override
            public void onDamageTaken(Character self, Character source, int damageTaken, List<String> log) {
                self.addPassive(new ExtraActionsPassive(1));
            }
        };
        Character defender = new Character("Dragon",
                new Stats(200, 10, 1, 10, 1, 10),
                Type.FIRE, List.of(), List.of(phasing));
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        alwaysHits().resolveAction(attacker, new ActionChoice(slash, List.of(defender)));

        assertTrue(defender.hasPassive(ExtraActionsPassive.class));
    }

    @Test
    public void aNewStatusDoesNotOverrideExistingStatuses() {
        Character caster = character("Caster", 200, 50, 50);
        Character enemy = character("Enemy", 400, 10, 10);
        Move poison = new Move("Toxin", Type.SHADOW, 10, 100, 0, false, Status.POISON, 100);
        Move burn = new Move("Ember", Type.FIRE, 10, 100, 0, false, Status.BURN, 100);

        alwaysHits().resolveAction(caster, new ActionChoice(poison, List.of(enemy)));
        alwaysHits().resolveAction(caster, new ActionChoice(burn, List.of(enemy)));

        assertTrue(enemy.hasStatus(Status.POISON));
        assertTrue(enemy.hasStatus(Status.BURN));
    }

    @Test
    public void reapplyingBleedIntensifiesInsteadOfExtendingDuration() {
        Character attacker = character("Knight", 200, 80, 0);
        Character enemy = character("Enemy", 400, 10, 10);
        Move rend = new Move("Rend", Type.PHYSICAL, 20, 100, 0, false, Status.BLEED, 100);

        alwaysHits().resolveAction(attacker, new ActionChoice(rend, List.of(enemy)));
        alwaysHits().resolveAction(attacker, new ActionChoice(rend, List.of(enemy)));

        assertTrue(enemy.hasStatus(Status.BLEED));
        assertEquals(3, enemy.getStatusTurnsRemaining(Status.BLEED));
        assertEquals(80, enemy.getStatusMagnitude(Status.BLEED));
        assertEquals(1.15, enemy.getStatusEffectiveness(Status.BLEED), 0.0001);
    }

    @Test
    public void stunSkipClearsOnlyStun() {
        Character stunned = character("Victim", 200, 10, 10);
        stunned.setStatus(Status.BURN);
        stunned.setStatus(Status.STUN);
        Character dummy = character("Dummy", 200, 10, 10);
        Move slash = new Move("Slash", Type.PHYSICAL, 20, 100, 0, false, Status.NONE, 0);

        List<String> log = alwaysHits().resolveAction(stunned, new ActionChoice(slash, List.of(dummy)));

        assertTrue(log.stream().anyMatch(line -> line.contains("stunned")));
        assertTrue(stunned.hasStatus(Status.BURN));
        assertFalse(stunned.hasStatus(Status.STUN));
    }
}
