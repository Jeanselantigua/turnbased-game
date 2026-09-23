package com.battlesim.ui;

import com.battlesim.dungeon.WaypointChoice;
import com.battlesim.model.Character;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Rest or chest between dungeon floors. */
final class WaypointView extends VBox {

    WaypointView(List<Character> party, int waveNumber, Consumer<WaypointChoice> onPick) {
        setSpacing(14);
        setPadding(new Insets(28));
        getStyleClass().add("screen");
        setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Waypoint — floor " + waveNumber);
        heading.getStyleClass().add("title");
        Label hint = new Label("Rest heals and revives. A chest pays gold and gear instead.");
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);
        getChildren().addAll(heading, hint);

        if (party != null) {
            for (Character member : party) {
                if (member == null || member.isSummon()) {
                    continue;
                }
                String hp = member.isFainted()
                        ? "fainted"
                        : member.getStats().getCurrentHp() + "/" + member.getStats().getMaxHp();
                Label line = new Label(member.getName() + "  " + hp);
                line.getStyleClass().add("camp-sheet");
                getChildren().add(line);
            }
        }

        Button rest = new Button("Rest (full heal / revive)");
        rest.getStyleClass().add("primary-button");
        rest.setOnAction(e -> onPick.accept(WaypointChoice.REST));
        Button chest = new Button("Open a chest");
        chest.getStyleClass().add("ghost-button");
        chest.setOnAction(e -> onPick.accept(WaypointChoice.CHEST));
        getChildren().addAll(rest, chest);
    }
}
