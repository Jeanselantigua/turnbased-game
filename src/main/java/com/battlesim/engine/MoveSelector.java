package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import java.util.List;

/**
 * Supplies a Move + target choice for a Character when it's their turn —
 * implemented differently for a human player (reads console input) vs
 * an AI opponent (picks randomly or by some strategy).
 */
public interface MoveSelector {
    ActionChoice chooseAction(Character actor, List<Move> availableMoves,
                               List<Character> enemyTeam, List<Character> allyTeam);
}
