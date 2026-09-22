package com.battlesim.model;

import com.battlesim.progress.Growth;
import java.util.ArrayList;
import java.util.List;

/**
 * A character's learnable moves. Playable kits are gated (1 / 6 / 12 / 18 ult).
 * Enemy kits are always known.
 */
public final class MoveKit {

    public static final int[] UNLOCK_LEVELS = {1, 6, 12, Growth.ULT_UNLOCK_LEVEL};

    private final List<Move> moves;
    private final boolean gated;

    private MoveKit(List<Move> moves, boolean gated) {
        this.moves = List.copyOf(moves);
        this.gated = gated;
    }

    public static MoveKit alwaysKnown(List<Move> moves) {
        if (moves == null) {
            throw new IllegalArgumentException("moves");
        }
        return new MoveKit(moves, false);
    }

    public static MoveKit alwaysKnown(Move... moves) {
        return alwaysKnown(List.of(moves));
    }

    /** Starter, 2nd at 6, 3rd at 12, ult at 18. */
    public static MoveKit ladder(Move first, Move second, Move third, Move ult) {
        if (first == null || second == null || third == null || ult == null) {
            throw new IllegalArgumentException("A playable kit needs four moves");
        }
        return new MoveKit(List.of(first, second, third, ult), true);
    }

    public List<Move> allMoves() {
        return moves;
    }

    public boolean isGated() {
        return gated;
    }

    public Move getUlt() {
        if (!gated || moves.size() < UNLOCK_LEVELS.length) {
            return null;
        }
        return moves.get(UNLOCK_LEVELS.length - 1);
    }

    public int unlockLevel(int index) {
        if (!gated) {
            return 1;
        }
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        if (index >= UNLOCK_LEVELS.length) {
            return UNLOCK_LEVELS[UNLOCK_LEVELS.length - 1];
        }
        return UNLOCK_LEVELS[index];
    }

    public List<Move> unlockedAt(int level) {
        if (!gated) {
            return moves;
        }
        List<Move> known = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            if (unlockLevel(i) <= level) {
                known.add(moves.get(i));
            }
        }
        return known;
    }
}
