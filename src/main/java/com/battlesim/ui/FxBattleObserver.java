package com.battlesim.ui;

import com.battlesim.engine.BattleObserver;
import com.battlesim.engine.TurnOrderScheduler.UpcomingTurn;
import com.battlesim.model.Character;
import java.util.List;
import javafx.application.Platform;

final class FxBattleObserver implements BattleObserver {

    private final BattleView view;

    FxBattleObserver(BattleView view) {
        this.view = view;
    }

    @Override
    public void onLog(List<String> lines) {
        Platform.runLater(() -> view.appendLog(lines));
    }

    @Override
    public void onField(List<Character> teamA, List<Character> teamB) {
        Platform.runLater(() -> view.showField(teamA, teamB));
    }

    @Override
    public void onTurnQueue(List<UpcomingTurn> upcoming, List<Character> allies) {
        Platform.runLater(() -> view.showQueue(upcoming, allies));
    }
}
