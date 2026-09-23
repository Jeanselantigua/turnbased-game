package com.battlesim.ui;

import com.battlesim.dungeon.Camp;
import com.battlesim.dungeon.WaypointChoice;
import com.battlesim.model.Character;
import com.battlesim.model.Inventory;
import com.battlesim.util.RandomProvider;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;

/** Blocks the dungeon thread on JavaFX camp / waypoint screens. */
final class FxCamp implements Camp {

    private final Consumer<Node> show;
    private final BattleView battleView;
    private final RandomProvider random;
    private volatile CompletableFuture<?> pending;

    FxCamp(Consumer<Node> show, BattleView battleView, RandomProvider random) {
        this.show = show;
        this.battleView = battleView;
        this.random = random;
    }

    void cancel() {
        CompletableFuture<?> current = pending;
        if (current != null) {
            current.cancel(true);
        }
    }

    @Override
    public WaypointChoice pickWaypoint(List<Character> party, Inventory inventory, int waveNumber) {
        return await(future -> show.accept(new WaypointView(party, waveNumber, choice -> {
            if (!future.isDone()) {
                future.complete(choice);
            }
        })));
    }

    @Override
    public void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves) {
        if (party == null || party.isEmpty() || inventory == null) {
            return;
        }
        await(future -> show.accept(new CampView(party, inventory, waveNumber, moreWaves, random, () -> {
            show.accept(battleView);
            if (!future.isDone()) {
                future.complete(null);
            }
        })));
    }

    private <T> T await(Consumer<CompletableFuture<T>> showScreen) {
        CompletableFuture<T> future = new CompletableFuture<>();
        pending = future;
        Platform.runLater(() -> showScreen.accept(future));
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw new RuntimeException("Camp interrupted", e);
        } catch (CancellationException | ExecutionException e) {
            throw new RuntimeException("Camp cancelled", e);
        }
    }
}
