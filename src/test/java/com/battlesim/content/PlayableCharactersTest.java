package com.battlesim.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Type;
import com.battlesim.content.passives.RogueWoundPassive;
import com.battlesim.model.StatKind;
import com.battlesim.progress.Growth;
import com.battlesim.util.RandomProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class PlayableCharactersTest {

    @Test
    public void allReturnsUniqueNamedTemplates() {
        List<CharacterTemplate> roster = PlayableCharacters.all();
        assertEquals(9, roster.size());
        Set<String> names = roster.stream()
                .map(CharacterTemplate::getName)
                .collect(Collectors.toSet());
        assertEquals(9, names.size());
        assertTrue(names.contains("Mechanized Champion"));
        assertTrue(names.contains("Rogue"));
        assertTrue(names.contains("Roeseph"));
        assertTrue(names.contains("Randy"));
        assertTrue(names.contains("Chefromancer"));
        assertTrue(names.contains("Okirik"));
        assertTrue(names.contains("Volt"));
        assertTrue(names.contains("Sion"));
        assertTrue(names.contains("Dual Swordsman"));
    }

    @Test
    public void everyPlayableHasAFourMoveLadder() {
        for (CharacterTemplate template : PlayableCharacters.all()) {
            assertEquals(template.getName() + " should have a 4-move kit",
                    4, template.getKit().allMoves().size());
            assertTrue(template.getKit().isGated());
            assertEquals(1, template.createInstance().getMoves().size());
            assertEquals(4, template.createFullyLearnedInstance().getMoves().size());
            assertTrue(template.getKit().getUlt().getCooldownTurns() >= 2);
        }
    }

    @Test
    public void rogueUltIsOnAShortCooldown() {
        assertEquals(RogueWoundPassive.ASSASSINATE_COOLDOWN,
                PlayableCharacters.rogue().getKit().getUlt().getCooldownTurns());
        assertEquals("Maim", PlayableCharacters.rogue().getKit().allMoves().get(2).getName());
        assertEquals("Electric Whirlwind",
                PlayableCharacters.dualSwordsman().getKit().getUlt().getName());
    }

    @Test
    public void monkUltIsVajrapaniAndHitsOneToTwelveTimes() {
        Move ult = PlayableCharacters.monk().getKit().getUlt();
        assertEquals("Vajrapani", ult.getName());
        assertEquals(Type.HOLY, ult.getType());
        assertTrue(ult.isMagic());
        assertEquals(1, ult.getMinHits());
        assertEquals(12, ult.getMaxHits());
        assertEquals(Growth.ULT_COOLDOWN_TURNS, ult.getCooldownTurns());

        Character monk = PlayableCharacters.monk().createFullyLearnedInstance();
        Character dummy = new Character("Dummy", new Stats(9999, 1, 1, 1, 1, 1),
                Type.PHYSICAL, List.of());
        RandomProvider rolls = new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                if (min == 1 && max == 12) {
                    return 12;
                }
                return min;
            }
        };
        TurnResolver resolver = new TurnResolver(new DamageCalculator(new TypeChart(), rolls), rolls);
        List<String> log = resolver.resolveAction(monk, new ActionChoice(ult, List.of(dummy)));
        assertTrue(log.stream().anyMatch(line -> line.contains("Vajrapani strikes 12 times")));
    }

    @Test
    public void startingStatsAreAboutHalfOfTheOldBaseline() {
        Character knight = PlayableCharacters.knight().createInstance();
        assertEquals(113, knight.getStats().getMaxHp());
        assertEquals(29, knight.getStats().getAttack());
        assertEquals(1, knight.getLevel());

        Character rogue = PlayableCharacters.rogue().createInstance();
        assertEquals(91, rogue.getStats().getMaxHp());
        assertEquals(30, rogue.getStats().getAttack());
        assertEquals(35, rogue.getStats().getSpeed());
    }

    @Test
    public void specialtiesMatchTheKit() {
        assertEquals(List.of(StatKind.HP, StatKind.DEFENSE),
                PlayableCharacters.knight().getSpecialties());
        assertEquals(List.of(StatKind.SPEED, StatKind.ATTACK),
                PlayableCharacters.rogue().getSpecialties());
        assertEquals(List.of(StatKind.HP, StatKind.ATTACK),
                PlayableCharacters.caveman().getSpecialties());
    }

    @Test
    public void arenaRosterKeepsPhase2StatsAndCurrentKits() {
        List<CharacterTemplate> arena = PlayableCharacters.arenaRoster();
        assertEquals(PlayableCharacters.all().size(), arena.size());

        Character knight = arena.get(0).createFullyLearnedInstance();
        assertEquals("Mechanized Champion", knight.getName());
        assertEquals(200, knight.getStats().getMaxHp());
        assertEquals(57, knight.getStats().getAttack());
        assertEquals(4, knight.getMoves().size());
        assertEquals(PlayableCharacters.knight().getKit().getUlt().getName(),
                knight.getKit().getUlt().getName());

        Character rogue = arena.get(1).createFullyLearnedInstance();
        assertEquals(200, rogue.getStats().getMaxHp());
        assertEquals(60, rogue.getStats().getAttack());
        assertEquals(70, rogue.getStats().getSpeed());
        assertEquals(4, rogue.getMoves().size());
        assertTrue(rogue.knowsMove("Assassinate"));

        Character dungeonRogue = PlayableCharacters.rogue().createInstance();
        assertEquals(91, dungeonRogue.getStats().getMaxHp());
        assertEquals(1, dungeonRogue.getMoves().size());

        Character dungeonChampion = PlayableCharacters.knight().createInstance();
        assertEquals(113, dungeonChampion.getStats().getMaxHp());
        assertEquals(29, dungeonChampion.getStats().getAttack());
        assertEquals(1, dungeonChampion.getMoves().size());
    }

    @Test
    public void arenaUltStartsOnCooldown() {
        for (CharacterTemplate template : PlayableCharacters.arenaRoster()) {
            Character fighter = template.createFullyLearnedInstance();
            fighter.putUltOnCooldown();
            Move ult = fighter.getKit().getUlt();
            assertEquals(template.getName() + " ult should start on cooldown",
                    ult.getCooldownTurns(), fighter.getMoveCooldown(ult.getName()));
        }
    }
}
