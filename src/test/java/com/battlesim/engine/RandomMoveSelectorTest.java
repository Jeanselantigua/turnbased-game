package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class RandomMoveSelectorTest {

    @Test
    public void picksAmongMultipleMovesAndTargets() {
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        Move bash = new Move("Shield Bash", Type.PHYSICAL, 35, 90, 0, false, Status.STUN, 30);
        Character actor = new Character("Knight", new Stats(280, 25, 40, 0, 25, 30),
                Type.PHYSICAL, List.of(slash, bash));
        Character t1 = new Character("A", new Stats(100, 10, 10, 10, 10, 10), Type.PHYSICAL, List.of(slash));
        Character t2 = new Character("B", new Stats(100, 10, 10, 10, 10, 10), Type.PHYSICAL, List.of(slash));
        Character t3 = new Character("C", new Stats(100, 10, 10, 10, 10, 10), Type.PHYSICAL, List.of(slash));
        List<Character> enemies = List.of(t1, t2, t3);

        RandomMoveSelector selector = new RandomMoveSelector(new RandomProvider(7L));
        Set<String> moves = new HashSet<>();
        Set<String> targets = new HashSet<>();
        for (int i = 0; i < 80; i++) {
            ActionChoice choice = selector.chooseAction(actor, enemies, List.of(actor));
            assertEquals(1, choice.getTargets().size());
            moves.add(choice.getMove().getName());
            targets.add(choice.getTargets().get(0).getName());
        }

        assertEquals(Set.of("Slash", "Shield Bash"), moves);
        assertEquals(Set.of("A", "B", "C"), targets);
    }
}
