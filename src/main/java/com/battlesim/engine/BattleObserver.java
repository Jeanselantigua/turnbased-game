package com.battlesim.engine;

import com.battlesim.model.Character;
import java.util.List;

/** Live battle callbacks for a UI (or tests). Console play can ignore this. */
public interface BattleObserver {

    default void onLog(List<String> lines) {
    }

    default void onField(List<Character> teamA, List<Character> teamB) {
    }

    default void onTurnQueue(List<TurnOrderScheduler.UpcomingTurn> upcoming, List<Character> allies) {
    }
}
