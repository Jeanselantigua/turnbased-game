package com.battlesim.ui;

import com.battlesim.model.Character;
import com.battlesim.model.Status;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class FighterCard extends VBox {

    private final Label name = new Label();
    private final Label hp = new Label();
    private final Label statuses = new Label();
    private final Region fill = new Region();
    private final Region track = new Region();
    private Character bound;
    private boolean enemy;

    FighterCard() {
        setSpacing(6);
        setPadding(new Insets(10));
        setMinWidth(160);
        setPrefWidth(180);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add("fighter-card");

        name.getStyleClass().add("fighter-name");
        hp.getStyleClass().add("fighter-hp");
        statuses.getStyleClass().add("fighter-status");
        statuses.setWrapText(true);

        track.getStyleClass().add("hp-track");
        fill.getStyleClass().add("hp-fill");
        fill.setMaxHeight(Double.MAX_VALUE);
        StackPane bar = new StackPane(track, fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPrefHeight(8);
        track.setPrefWidth(160);
        fill.setPrefHeight(8);

        getChildren().addAll(name, bar, hp, statuses);
    }

    void bind(Character character, boolean enemySide, Character actor) {
        this.bound = character;
        this.enemy = enemySide;
        refresh(actor);
    }

    void refresh(Character actor) {
        if (bound == null) {
            return;
        }
        boolean acting = bound == actor;
        setCardClass("enemy", enemy);
        setCardClass("acting", acting);
        name.setText(bound.getName() + "  Lv" + bound.getLevel());
        int cur = bound.getStats().getCurrentHp();
        int max = bound.getStats().getMaxHp();
        hp.setText(cur + " / " + max + " HP");
        double pct = max <= 0 ? 0 : Math.max(0, Math.min(1, cur / (double) max));
        fill.setPrefWidth(Math.max(4, 160 * pct));
        fill.setStyle("-fx-background-color: " + (enemy ? UiTheme.ENEMY : UiTheme.ALLY) + ";");
        StringBuilder statusText = new StringBuilder();
        for (Status status : bound.getActiveStatuses()) {
            if (status == Status.NONE || status == Status.UTILITY) {
                continue;
            }
            if (statusText.length() > 0) {
                statusText.append("  ");
            }
            statusText.append(status.name());
        }
        statuses.setText(statusText.length() == 0 ? " " : statusText.toString());
        setOpacity(bound.isFainted() ? 0.45 : 1);
    }

    private void setCardClass(String styleClass, boolean on) {
        if (on) {
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        } else {
            getStyleClass().remove(styleClass);
        }
    }
}
