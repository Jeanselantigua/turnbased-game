package com.battlesim.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.passives.MonkMasterOfAnyArtPassive;
import com.battlesim.content.passives.RogueWoundPassive;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.StatKind;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
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
    public void monkUltIsMasterOfAnyArt() {
        Move ult = PlayableCharacters.monk().getKit().getUlt();
        assertEquals(MonkMasterOfAnyArtPassive.MOVE_NAME, ult.getName());
        assertEquals(Type.HOLY, ult.getType());
        assertTrue(ult.isMagic());
        assertEquals(0, ult.getPower());
        assertEquals(Growth.ULT_COOLDOWN_TURNS, ult.getCooldownTurns());
        assertTrue(PlayableCharacters.monk().createFullyLearnedInstance()
                .hasPassive(MonkMasterOfAnyArtPassive.class));
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
    public void pvpInstanceKnowsFullKitWithUltOnCooldown() {
        for (CharacterTemplate template : PlayableCharacters.arenaRoster()) {
            Character fighter = template.createPvpInstance();
            assertEquals(template.getName() + " PvP kit", 4, fighter.getMoves().size());
            Move ult = fighter.getKit().getUlt();
            assertEquals(template.getName() + " ult starts on its cooldown",
                    ult.getCooldownTurns(), fighter.getMoveCooldown(ult.getName()));
            for (int i = 0; i < ult.getCooldownTurns(); i++) {
                fighter.tickMoveCooldowns();
            }
            assertEquals(template.getName() + " ult should be ready after its cooldown",
                    0, fighter.getMoveCooldown(ult.getName()));
        }
        Character rogue = PlayableCharacters.arenaRoster().get(1).createPvpInstance();
        assertEquals(200, rogue.getStats().getMaxHp());
        assertEquals(60, rogue.getStats().getAttack());
        assertEquals(70, rogue.getStats().getSpeed());
        Character dungeonRogue = PlayableCharacters.rogue().createPvpInstance();
        assertTrue(dungeonRogue.getStats().getMaxHp() < rogue.getStats().getMaxHp());
    }

    @Test
    public void arenaUltStartsOnCooldown() {
        for (CharacterTemplate template : PlayableCharacters.arenaRoster()) {
            Character fighter = template.createPvpInstance();
            Move ult = fighter.getKit().getUlt();
            assertEquals(template.getName() + " ult should start on cooldown",
                    ult.getCooldownTurns(), fighter.getMoveCooldown(ult.getName()));
        }
    }
}
