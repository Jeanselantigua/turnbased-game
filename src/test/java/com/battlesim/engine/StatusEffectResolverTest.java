package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StatusEffectResolverTest {

    private Character character(String name, int maxHp, int attack, int magicAttack) {
        Stats stats = new Stats(maxHp, attack, 10, magicAttack, 10, 10);
        return new Character(name, stats, Type.PHYSICAL, List.of());
    }

    @Test
    public void cursedDealsFifteenPercentMagicAttackAndExpiresAfterThreeTurns() {
        Character mage = character("Mage", 200, 10, 100);
        mage.setStatus(Status.CURSED);
        assertEquals(3, mage.getStatusTurnsRemaining());

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();

        resolver.applyStartOfTurnEffects(mage, log);
        assertEquals(185, mage.getStats().getCurrentHp());
        assertEquals(Status.CURSED, mage.getStatus());
        assertEquals(2, mage.getStatusTurnsRemaining());

        resolver.applyStartOfTurnEffects(mage, log);
        assertEquals(170, mage.getStats().getCurrentHp());
        assertEquals(1, mage.getStatusTurnsRemaining());

        resolver.applyStartOfTurnEffects(mage, log);
        assertEquals(155, mage.getStats().getCurrentHp());
        assertEquals(Status.NONE, mage.getStatus());
        assertEquals(0, mage.getStatusTurnsRemaining());
        assertTrue(log.stream().anyMatch(line -> line.contains("no longer cursed")));
    }

    @Test
    public void slowHalvesSpeedAndExpiresAfterThreeTurnsWithoutDamage() {
        Character target = character("Target", 200, 10, 10);
        target.setStatus(Status.SLOW);
        assertEquals(3, target.getStatusTurnsRemaining());
        assertEquals(5, target.getEffectiveSpeed());

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();

        resolver.applyStartOfTurnEffects(target, log);
        assertEquals(200, target.getStats().getCurrentHp());
        assertEquals(Status.SLOW, target.getStatus());
        assertEquals(2, target.getStatusTurnsRemaining());
        assertEquals(5, target.getEffectiveSpeed());

        resolver.applyStartOfTurnEffects(target, log);
        resolver.applyStartOfTurnEffects(target, log);
        assertEquals(Status.NONE, target.getStatus());
        assertEquals(10, target.getEffectiveSpeed());
        assertTrue(log.stream().anyMatch(line -> line.contains("no longer slowed")));
    }

    @Test
    public void aftermathDamagesKillerForFifteenPercentOfFaintedMaxHp() {
        Character bomber = character("Bomber", 200, 10, 10);
        Character killer = character("Slayer", 100, 10, 10);
        bomber.setStatus(Status.AFTERMATH);
        bomber.getStats().applyDamage(200);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyOnFaintEffects(bomber, killer, log);

        assertEquals(70, killer.getStats().getCurrentHp());
        assertTrue(log.get(0).contains("aftermath"));
    }

    @Test
    public void aftermathDoesNotDamageAnAlreadyFaintedKiller() {
        Character bomber = character("Bomber", 200, 10, 10);
        Character killer = character("Slayer", 100, 10, 10);
        bomber.setStatus(Status.AFTERMATH);
        bomber.getStats().applyDamage(200);
        killer.getStats().applyDamage(100);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyOnFaintEffects(bomber, killer, log);

        assertEquals(0, killer.getStats().getCurrentHp());
        assertTrue(log.isEmpty());
    }

    @Test
    public void healRestoresTwentyFivePercentOfTargetMaxHp() {
        Character wounded = character("Wounded", 200, 10, 10);
        wounded.getStats().applyDamage(100);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyHeal(wounded, log);

        assertEquals(150, wounded.getStats().getCurrentHp());
        assertTrue(log.get(0).contains("recovers 50 HP"));
    }

    @Test
    public void healDoesNotExceedMaxHp() {
        Character nearlyFull = character("NearlyFull", 200, 10, 10);
        nearlyFull.getStats().applyDamage(10);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyHeal(nearlyFull, log);

        assertEquals(200, nearlyFull.getStats().getCurrentHp());
        assertTrue(log.get(0).contains("recovers 10 HP"));
    }

    @Test
    public void shieldGrantsMovePowerAsShieldHp() {
        Character target = character("Target", 200, 10, 10);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyShield(target, 100, log);

        assertEquals(100, target.getStats().getShieldHp());
        assertEquals(200, target.getStats().getCurrentHp());
        assertTrue(log.get(0).contains("100 HP shield"));
    }

    @Test
    public void shieldAbsorbsDamageThenLeftoverHitsHp() {
        Character target = character("Target", 200, 10, 10);
        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyShield(target, 40, log);

        int hpLost = resolver.applyDamageThroughShield(target, null, 70, null, log);

        assertEquals(0, target.getStats().getShieldHp());
        assertEquals(30, hpLost);
        assertEquals(170, target.getStats().getCurrentHp());
        assertTrue(log.stream().anyMatch(line -> line.contains("shield absorbs 40")));
        assertTrue(log.stream().anyMatch(line -> line.contains("shatters")));
    }

    @Test
    public void leechConvertsFifteenPercentOfDamageDealtToHealth() {
        Character attacker = character("Leech", 200, 10, 10);
        attacker.getStats().applyDamage(50);
        Move leech = new Move("Leech", Type.HOLY, 60, 90, 0, true, Status.LEECH, 100);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyLeechOnDamage(attacker, leech, 100, log);

        assertEquals(165, attacker.getStats().getCurrentHp());
        assertTrue(log.get(0).contains("leeches 15 HP"));
    }

    @Test
    public void leechDoesNotTriggerOnNonLeechMoves() {
        Character attacker = character("Slasher", 200, 10, 10);
        attacker.getStats().applyDamage(50);
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyLeechOnDamage(attacker, slash, 100, log);

        assertEquals(150, attacker.getStats().getCurrentHp());
        assertTrue(log.isEmpty());
    }

    @Test
    public void siphonDamagesTargetAndHealsCasterThenExpires() {
        Character chef = character("Chefromancer", 200, 21, 60);
        chef.getStats().applyDamage(40);
        Character victim = character("Victim", 200, 10, 10);
        victim.applySiphon(chef, 3);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();

        resolver.applyStartOfTurnEffects(victim, log);
        assertEquals(184, victim.getStats().getCurrentHp());
        assertEquals(168, chef.getStats().getCurrentHp());
        assertEquals(2, victim.getSiphonTurnsRemaining());
        assertTrue(log.stream().anyMatch(line -> line.contains("siphons")));

        resolver.applyStartOfTurnEffects(victim, log);
        resolver.applyStartOfTurnEffects(victim, log);
        assertEquals(Status.NONE, victim.getStatus());
        assertEquals(0, victim.getSiphonTurnsRemaining());
        assertTrue(log.stream().anyMatch(line -> line.contains("no longer siphoned")));
    }

    @Test
    public void siphonCanStackWithCursed() {
        Character chef = character("Chefromancer", 200, 21, 60);
        Character victim = character("Victim", 200, 10, 100);
        victim.setStatus(Status.CURSED);
        victim.applySiphon(chef, 3);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyStartOfTurnEffects(victim, log);

        assertEquals(Status.CURSED, victim.getStatus());
        assertTrue(victim.isSiphoned());
        assertEquals(169, victim.getStats().getCurrentHp());
    }

    @Test
    public void bleedTickUsesInflictorAttackSnapshotNotAfflictedAttack() {
        Character victim = character("Victim", 200, 10, 10);
        victim.setStatus(Status.BLEED, 100);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyStartOfTurnEffects(victim, log);

        assertEquals(160, victim.getStats().getCurrentHp());
        assertEquals(Status.BLEED, victim.getStatus());
        assertEquals(2, victim.getStatusTurnsRemaining());
        assertEquals(100, victim.getStatusMagnitude());
        assertTrue(log.stream().anyMatch(line -> line.contains("bleed")));
    }

    @Test
    public void bleedExpiresAndClearsMagnitudeAfterThreeTurns() {
        Character victim = character("Victim", 200, 10, 10);
        victim.setStatus(Status.BLEED, 50);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyStartOfTurnEffects(victim, log);
        resolver.applyStartOfTurnEffects(victim, log);
        resolver.applyStartOfTurnEffects(victim, log);

        assertEquals(140, victim.getStats().getCurrentHp());
        assertEquals(Status.NONE, victim.getStatus());
        assertEquals(0, victim.getStatusMagnitude());
        assertTrue(log.stream().anyMatch(line -> line.contains("no longer bleeding")));
    }

    @Test
    public void multipleDotsTickTogetherOnTheVictimsTurn() {
        Character victim = character("Victim", 200, 10, 100);
        victim.setStatus(Status.BURN);
        victim.setStatus(Status.POISON);
        victim.setStatus(Status.BLEED, 100);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyStartOfTurnEffects(victim, log);

        assertEquals(124, victim.getStats().getCurrentHp());
        assertTrue(victim.hasStatus(Status.BURN));
        assertTrue(victim.hasStatus(Status.POISON));
        assertTrue(victim.hasStatus(Status.BLEED));
        assertTrue(log.stream().anyMatch(line -> line.contains("burn")));
        assertTrue(log.stream().anyMatch(line -> line.contains("poison")));
        assertTrue(log.stream().anyMatch(line -> line.contains("bleed")));
    }

    @Test
    public void reapplyingBurnRaisesEffectivenessWithoutExtendingDuration() {
        Character victim = character("Victim", 200, 10, 10);
        victim.setStatus(Status.BURN);

        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyStartOfTurnEffects(victim, log);
        assertEquals(180, victim.getStats().getCurrentHp());
        assertEquals(2, victim.getStatusTurnsRemaining(Status.BURN));

        victim.applyStatus(Status.BURN, 0);
        assertEquals(2, victim.getStatusTurnsRemaining(Status.BURN));
        assertEquals(1.15, victim.getStatusEffectiveness(Status.BURN), 0.0001);

        resolver.applyStartOfTurnEffects(victim, log);
        assertEquals(157, victim.getStats().getCurrentHp());
        assertEquals(1, victim.getStatusTurnsRemaining(Status.BURN));
    }

    @Test
    public void reapplyingCurseResetsDurationToAFreshCopy() {
        Character victim = character("Victim", 200, 10, 100);
        victim.setStatus(Status.CURSED);

        StatusEffectResolver resolver = new StatusEffectResolver();
        resolver.applyStartOfTurnEffects(victim, new ArrayList<>());
        assertEquals(2, victim.getStatusTurnsRemaining(Status.CURSED));

        victim.applyStatus(Status.CURSED, 0);
        assertEquals(3, victim.getStatusTurnsRemaining(Status.CURSED));
        assertEquals(1.0, victim.getStatusEffectiveness(Status.CURSED), 0.0001);
    }

    @Test
    public void reapplyingSlowRaisesPenaltyWithoutExtendingDuration() {
        Character victim = character("Victim", 200, 10, 10);
        victim.setStatus(Status.SLOW);
        assertEquals(5, victim.getEffectiveSpeed());

        StatusEffectResolver resolver = new StatusEffectResolver();
        resolver.applyStartOfTurnEffects(victim, new ArrayList<>());
        assertEquals(2, victim.getStatusTurnsRemaining(Status.SLOW));

        victim.applyStatus(Status.SLOW, 0);
        assertEquals(2, victim.getStatusTurnsRemaining(Status.SLOW));
        assertEquals(4, victim.getEffectiveSpeed());
    }

    @Test
    public void reapplyingParalysisResetsDuration() {
        Character victim = character("Victim", 200, 10, 10);
        victim.setStatus(Status.PARALYSIS);

        StatusEffectResolver resolver = new StatusEffectResolver();
        resolver.applyStartOfTurnEffects(victim, new ArrayList<>());
        assertEquals(2, victim.getStatusTurnsRemaining(Status.PARALYSIS));

        victim.applyStatus(Status.PARALYSIS, 2);
        assertEquals(3, victim.getStatusTurnsRemaining(Status.PARALYSIS));
        assertEquals(2, victim.getStatusMagnitude(Status.PARALYSIS));
        assertEquals(0, victim.getQueuedSkipTurns());
    }

    @Test
    public void shieldReplacesExistingShieldInsteadOfStacking() {
        Character target = character("Target", 200, 10, 10);
        StatusEffectResolver resolver = new StatusEffectResolver();
        List<String> log = new ArrayList<>();
        resolver.applyShield(target, 40, log);
        resolver.applyShield(target, 100, log);
        assertEquals(100, target.getStats().getShieldHp());
    }
}
