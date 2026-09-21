package com.battlesim.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.CharacterTemplate;
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
        assertTrue(names.contains("Knight"));
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
    public void everyCharacterHasAtLeastTwoMoves() {
        for (CharacterTemplate template : PlayableCharacters.all()) {
            assertTrue(template.getName() + " should have at least two moves",
                    template.createInstance().getMoves().size() >= 2);
        }
    }
}
