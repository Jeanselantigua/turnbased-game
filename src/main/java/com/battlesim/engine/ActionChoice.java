package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import java.util.List;

/** What a MoveSelector decided: which Move, and who it targets. */
public class ActionChoice {

    private final Move move;
    private final List<Character> targets;

    public ActionChoice(Move move, List<Character> targets) {
        this.move = move;
        this.targets = targets;
    }

    public Move getMove() { return move; }
    public List<Character> getTargets() { return targets; }
}
