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

        resolver.applyEndOfTurnEffects(mage, log);
        assertEquals(185, mage.getStats().getCurrentHp());
        assertEquals(Status.CURSED, mage.getStatus());
        assertEquals(2, mage.getStatusTurnsRemaining());

        resolver.applyEndOfTurnEffects(mage, log);
        assertEquals(170, mage.getStats().getCurrentHp());
        assertEquals(1, mage.getStatusTurnsRemaining());

        resolver.applyEndOfTurnEffects(mage, log);
        assertEquals(155, mage.getStats().getCurrentHp());
        assertEquals(Status.NONE, mage.getStatus());
        assertEquals(0, mage.getStatusTurnsRemaining());
        assertTrue(log.stream().anyMatch(line -> line.contains("no longer cursed")));
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

        resolver.applyEndOfTurnEffects(victim, log);
        assertEquals(184, victim.getStats().getCurrentHp());
        assertEquals(168, chef.getStats().getCurrentHp());
        assertEquals(2, victim.getSiphonTurnsRemaining());
        assertTrue(log.stream().anyMatch(line -> line.contains("siphons")));

        resolver.applyEndOfTurnEffects(victim, log);
        resolver.applyEndOfTurnEffects(victim, log);
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
        resolver.applyEndOfTurnEffects(victim, log);

        assertEquals(Status.CURSED, victim.getStatus());
        assertTrue(victim.isSiphoned());
        assertEquals(169, victim.getStats().getCurrentHp());
    }
}
