package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.util.RandomProvider;
import java.util.List;

/** Picks a random move and a random target from the matching pool. */
public class RandomMoveSelector implements MoveSelector {

    private final RandomProvider random;

    public RandomMoveSelector(RandomProvider random) {
        this.random = random;
    }

    @Override
    public ActionChoice chooseAction(Character actor, List<Move> availableMoves,
                                      List<Character> enemyTeam, List<Character> allyTeam) {
        List<Move> moves = availableMoves.isEmpty() ? actor.getMoves() : availableMoves;
        Move chosenMove = moves.get(random.nextInt(0, moves.size() - 1));
        List<Character> pool = chosenMove.legalTargets(actor, allyTeam, enemyTeam);
        Character chosenTarget = pool.get(random.nextInt(0, pool.size() - 1));
        return new ActionChoice(chosenMove, List.of(chosenTarget));
    }
}
