package com.battlesim.ui;

import com.battlesim.content.PlayableCharacters;
import com.battlesim.model.Character;
import com.battlesim.model.CharacterTemplate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

final class TeamPickView extends VBox {

    private final Label heading = new Label();
    private final Label hint = new Label("Pick 1 to 3 unique fighters.");
    private final FlowPane roster = new FlowPane();
    private final Button confirm = new Button("Continue");
    private final Button back = new Button("Back");
    private final Set<String> selected = new LinkedHashSet<>();
    private final int slots;
    private final boolean pvpKit;

    TeamPickView(String title, int slots, boolean pvpKit, Runnable onBack, Consumer<List<Character>> onConfirm) {
        this.slots = slots;
        this.pvpKit = pvpKit;
        heading.setText(title);
        heading.getStyleClass().add("title");
        hint.getStyleClass().add("muted");
        if (pvpKit) {
            hint.setText("Pick 1 to 3 unique fighters. Full kits; ults start on cooldown.");
        }
        confirm.getStyleClass().add("primary-button");
        back.getStyleClass().add("ghost-button");
        roster.setHgap(10);
        roster.setVgap(10);
        setSpacing(16);
        setPadding(new Insets(28));
        getStyleClass().add("screen");
        setAlignment(Pos.TOP_LEFT);

        for (CharacterTemplate template : roster()) {
            Button card = new Button(template.getName());
            card.getStyleClass().add("roster-card");
            card.setOnAction(e -> toggle(template.getName(), card));
            roster.getChildren().add(card);
        }

        confirm.setDisable(true);
        confirm.setOnAction(e -> onConfirm.accept(createTeam()));
        back.setOnAction(e -> onBack.run());
        getChildren().addAll(heading, hint, roster, confirm, back);
    }

    private void toggle(String name, Button card) {
        if (selected.contains(name)) {
            selected.remove(name);
            card.getStyleClass().remove("selected");
        } else if (selected.size() < slots) {
            selected.add(name);
            card.getStyleClass().add("selected");
        }
        confirm.setDisable(selected.isEmpty());
        hint.setText(selected.size() + " / " + slots + " selected");
    }

    private List<Character> createTeam() {
        List<Character> team = new ArrayList<>();
        for (CharacterTemplate template : roster()) {
            if (selected.contains(template.getName())) {
                team.add(pvpKit ? template.createPvpInstance() : template.createInstance());
            }
        }
        return team;
    }

    private List<CharacterTemplate> roster() {
        return pvpKit ? PlayableCharacters.arenaRoster() : PlayableCharacters.all();
    }
}
