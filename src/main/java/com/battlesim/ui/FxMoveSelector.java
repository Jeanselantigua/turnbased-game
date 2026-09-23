package com.battlesim.ui;

import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.MoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;

final class FxMoveSelector implements MoveSelector {

    private final BattleView view;
    private volatile CompletableFuture<ActionChoice> pending;

    FxMoveSelector(BattleView view) {
        this.view = view;
    }

    void cancel() {
        CompletableFuture<ActionChoice> current = pending;
        if (current != null) {
            current.cancel(true);
        }
        Platform.runLater(view::cancelPrompt);
    }

    @Override
    public ActionChoice chooseAction(Character actor, List<Move> availableMoves,
                                     List<Character> enemyTeam, List<Character> allyTeam) {
        CompletableFuture<ActionChoice> future = new CompletableFuture<>();
        pending = future;
        Platform.runLater(() -> view.ask(actor, availableMoves, enemyTeam, allyTeam, future));
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Battle interrupted", e);
        } catch (CancellationException | ExecutionException e) {
            throw new RuntimeException("Battle cancelled", e);
        }
    }
}
