package com.battlesim.content.passives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class RogueCritStealthPassiveTest {

    private static final Move BACKSTAB = new Move("BackStab", Type.PHYSICAL, 40, 90, 1, false,
            Status.NONE, 0);

    @Test
    public void templateReplacesCrossbowWithAmbush() {
        Character rogue = PlayableCharacters.rogue().createFullyLearnedInstance();
        assertTrue(rogue.hasPassive(RogueCritStealthPassive.class));
        assertTrue(rogue.knowsMove(RogueCritStealthPassive.AMBUSH_NAME));
        assertFalse(rogue.knowsMove("Crossbow"));
        assertEquals(RogueCritStealthPassive.AMBUSH_NAME,
                PlayableCharacters.rogue().getKit().allMoves().get(1).getName());
    }

    @Test
    public void ambushIsHiddenUntilStealthed() {
        RogueCritStealthPassive stealth = new RogueCritStealthPassive();
        Character rogue = PlayableCharacters.rogue().createFullyLearnedInstance();
        List<Move> visible = stealth.filterOwnMoves(rogue, rogue.getMoves(), null);
        assertFalse(visible.stream().anyMatch(move -> RogueCritStealthPassive.AMBUSH_NAME.equals(move.getName())));

        stealth.setStealthed(true);
        List<Move> fromStealth = stealth.filterOwnMoves(rogue, rogue.getMoves(), null);
        assertTrue(fromStealth.stream().anyMatch(move -> RogueCritStealthPassive.AMBUSH_NAME.equals(move.getName())));
    }

    @Test
    public void ambushCritsMoreOftenThanOtherMoves() {
        RandomProvider midRoll = new RandomProvider() {
            @Override
            public double nextDouble() {
                return 0.20;
            }
        };
        RogueCritStealthPassive stealth = new RogueCritStealthPassive(midRoll);
        Character rogue = new Character("Rogue", new Stats(200, 40, 10, 10, 10, 70),
                Type.ARCANE, List.of(BACKSTAB, RogueCritStealthPassive.createAmbush()), List.of(stealth));

        assertFalse(stealth.rollBonusCrit(rogue, BACKSTAB));
        assertEquals(15, rogue.getCritRate(BACKSTAB));
        assertEquals(45, rogue.getCritRate(RogueCritStealthPassive.createAmbush()));
        assertEquals(100, rogue.getCritDamage());
    }

    @Test
    public void aiUsesAmbushWhenItIsAvailable() {
        Character rogue = PlayableCharacters.rogue().createFullyLearnedInstance();
        RogueCritStealthPassive stealth = rogue.getPassive(RogueCritStealthPassive.class);
        stealth.setStealthed(true);
        Character foe = new Character("Foe", new Stats(200, 10, 10, 10, 10, 10),
                Type.PHYSICAL, List.of());
        List<Move> available = stealth.filterOwnMoves(rogue, rogue.getMoves(), null);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(rogue, available, List.of(foe), List.of(rogue));
        assertEquals(RogueCritStealthPassive.AMBUSH_NAME, choice.getMove().getName());
        assertEquals(foe, choice.getTargets().get(0));
    }
}
