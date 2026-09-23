package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ChampionsGiftPassiveTest {

    private static final Move SLASH = new Move("Slash", Type.PHYSICAL, 60, 100, 0, false, Status.NONE, 0);

    private static RandomProvider alwaysCrits() {
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

    private static RandomProvider neverCrits() {
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

    private static TurnResolver alwaysHits() {
        return new TurnResolver(new DamageCalculator(new TypeChart(), alwaysCrits()), alwaysCrits());
    }

    private static Character champion(ChampionsGiftPassive gift) {
        return new Character("Mechanized Champion", new Stats(200, 50, 10, 0, 10, 20),
                Type.PHYSICAL, List.of(SLASH), List.of(gift));
    }

    private static Character dummy() {
        return new Character("Dummy", new Stats(99999, 1, 10, 1, 10, 10),
                Type.PHYSICAL, List.of());
    }

    private static Character ally() {
        return new Character("Ally", new Stats(200, 50, 10, 0, 10, 20),
                Type.PHYSICAL, List.of(SLASH), List.of());
    }

    private static void dropBelowHalf(Character champion, ChampionsGiftPassive gift) {
        champion.getStats().applyDamage(101);
        gift.onDamageTaken(champion, dummy(), 101, new ArrayList<>());
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

    @Test
    public void giftStillAwakensOnItsOwnCharacter() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        assertTrue(champion.hasPassive(ChampionsGiftPassive.class));
        assertEquals("Mechanized Champion", champion.getName());
    }

    @Test
    public void staysDormantAtOrAboveHalfHealth() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        champion.getStats().applyDamage(100);
        gift.onDamageTaken(champion, dummy(), 100, new ArrayList<>());
        assertFalse(gift.isAwakened());
        assertEquals(0, champion.getCritRate());
        assertFalse(gift.rollBonusCrit(champion, SLASH));
    }

    @Test
    public void awakensBelowHalfHealth() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        List<String> log = new ArrayList<>();
        champion.getStats().applyDamage(101);
        gift.onDamageTaken(champion, dummy(), 101, log);
        assertTrue(gift.isAwakened());
        assertEquals(ChampionsGiftPassive.BASE_CRIT_CHANCE, gift.getCritChance(), 0.0001);
        assertTrue(log.stream().anyMatch(line -> line.contains("Champion's Gift")));
    }

    @Test
    public void fourGiftCritsThenCooldown() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        Character dummy = dummy();
        dropBelowHalf(champion, gift);
        TurnResolver resolver = alwaysHits();

        for (int i = 0; i < ChampionsGiftPassive.MAX_GIFT_CRITS; i++) {
            List<String> log = resolver.resolveAction(champion, new ActionChoice(SLASH, List.of(dummy)));
            assertTrue(log.stream().anyMatch(line -> line.contains("Champion's Gift")));
        }

        assertFalse(gift.isAwakened());
        assertEquals(ChampionsGiftPassive.COOLDOWN_TURNS, gift.getCooldownTurns());
        assertEquals(ChampionsGiftPassive.MAX_GIFT_CRITS, gift.getRemainingGiftCrits());
        assertEquals(0, champion.getCritRate());
    }

    @Test
    public void nonCritsRaiseTheSharedChance() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(neverCrits());
        Character champion = champion(gift);
        Character dummy = dummy();
        dropBelowHalf(champion, gift);
        TurnResolver resolver = new TurnResolver(
                new DamageCalculator(new TypeChart(), neverCrits()), neverCrits());

        resolver.resolveAction(champion, new ActionChoice(SLASH, List.of(dummy)));
        assertEquals(ChampionsGiftPassive.BASE_CRIT_CHANCE + ChampionsGiftPassive.CRIT_CHANCE_PER_NON_CRIT,
                gift.getCritChance(), 0.0001);
        assertEquals(ChampionsGiftPassive.MAX_GIFT_CRITS, gift.getRemainingGiftCrits());
    }

    @Test
    public void alliesShareTheGiftCrit() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        Character ally = ally();
        Character dummy = dummy();
        dropBelowHalf(champion, gift);

        List<String> log = alwaysHits().resolveAction(ally, new ActionChoice(SLASH, List.of(dummy)),
                party(champion, ally, dummy));

        assertTrue(log.stream().anyMatch(line -> line.contains("Champion's Gift")));
        assertEquals(ChampionsGiftPassive.MAX_GIFT_CRITS - 1, gift.getRemainingGiftCrits());
    }

    @Test
    public void cooldownLastsTwoChampionTurnsThenReawakens() {
        ChampionsGiftPassive gift = new ChampionsGiftPassive(alwaysCrits());
        Character champion = champion(gift);
        Character dummy = dummy();
        dropBelowHalf(champion, gift);
        TurnResolver resolver = alwaysHits();
        for (int i = 0; i < ChampionsGiftPassive.MAX_GIFT_CRITS; i++) {
            resolver.resolveAction(champion, new ActionChoice(SLASH, List.of(dummy)));
        }
        assertEquals(2, gift.getCooldownTurns());

        gift.onTurnStart(champion, null, new ArrayList<>());
        assertFalse(gift.isAwakened());
        assertEquals(1, gift.getCooldownTurns());

        List<String> log = new ArrayList<>();
        gift.onTurnStart(champion, null, log);
        assertTrue(gift.isAwakened());
        assertEquals(0, gift.getCooldownTurns());
        assertTrue(log.stream().anyMatch(line -> line.contains("blade comes to life")));
    }
}
