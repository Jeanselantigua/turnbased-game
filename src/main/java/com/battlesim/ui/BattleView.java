package com.battlesim.ui;

import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.TurnOrderScheduler.UpcomingTurn;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

final class BattleView extends BorderPane {

    private final Label banner = new Label("Battle");
    private final FlowPane enemiesBox = new FlowPane();
    private final FlowPane alliesBox = new FlowPane();
    private final TextArea log = new TextArea();
    private final Label prompt = new Label("Waiting for the next turn...");
    private final HBox actions = new HBox(8);
    private final Button home = new Button("Back to title");
    private final VBox queueRows = new VBox(4);
    private final List<FighterCard> enemyCards = new ArrayList<>();
    private final List<FighterCard> allyCards = new ArrayList<>();
    private Character actor;
    private Runnable onHome = () -> { };

    BattleView() {
        getStyleClass().add("screen");
        banner.getStyleClass().add("banner");
        prompt.getStyleClass().add("prompt");
        log.setEditable(false);
        log.setWrapText(true);
        log.setPrefRowCount(12);
        log.getStyleClass().add("combat-log");
        home.getStyleClass().add("ghost-button");
        home.setVisible(false);
        home.setOnAction(e -> onHome.run());

        enemiesBox.setHgap(10);
        enemiesBox.setVgap(10);
        alliesBox.setHgap(10);
        alliesBox.setVgap(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label enemyLabel = new Label("Enemies");
        enemyLabel.getStyleClass().add("section-label");
        Label allyLabel = new Label("Your team");
        allyLabel.getStyleClass().add("section-label");

        VBox top = new VBox(8, banner, enemyLabel, enemiesBox);
        VBox bottom = new VBox(8, allyLabel, alliesBox, prompt, actions, home);
        ScrollPane logPane = new ScrollPane(log);
        logPane.setFitToWidth(true);
        logPane.setFitToHeight(true);
        logPane.getStyleClass().add("log-pane");
        VBox.setVgrow(logPane, Priority.ALWAYS);

        VBox center = new VBox(12, logPane);
        Label queueLabel = new Label("Turn order");
        queueLabel.getStyleClass().add("section-label");
        Label queueHint = new Label("AV until they act");
        queueHint.getStyleClass().add("muted");
        queueRows.getStyleClass().add("queue-rows");
        ScrollPane queuePane = new ScrollPane(queueRows);
        queuePane.setFitToWidth(true);
        queuePane.getStyleClass().add("queue-pane");
        VBox.setVgrow(queuePane, Priority.ALWAYS);
        VBox queueBox = new VBox(8, queueLabel, queueHint, queuePane);
        queueBox.getStyleClass().add("queue-box");
        queueBox.setPrefWidth(220);
        queueBox.setMinWidth(200);

        setTop(top);
        setCenter(center);
        setRight(queueBox);
        setBottom(bottom);
        setPadding(new Insets(16));
        BorderPane.setMargin(center, new Insets(12, 12, 12, 0));
        BorderPane.setMargin(queueBox, new Insets(12, 0, 12, 0));
    }

    void setOnHome(Runnable onHome) {
        this.onHome = onHome != null ? onHome : () -> { };
    }

    void setBanner(String text) {
        banner.setText(text);
    }

    void reset() {
        log.clear();
        actions.getChildren().clear();
        home.setVisible(false);
        prompt.setText("Waiting for the next turn...");
        actor = null;
        queueRows.getChildren().clear();
    }

    void appendLog(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            if (!log.getText().isEmpty()) {
                log.appendText("\n");
            }
            log.appendText(line);
        }
        log.positionCaret(log.getLength());
    }

    void showField(List<Character> teamA, List<Character> teamB) {
        rebuild(allyCards, alliesBox, teamA, false);
        rebuild(enemyCards, enemiesBox, teamB, true);
        refreshCards();
    }

    void showQueue(List<UpcomingTurn> upcoming, List<Character> allies) {
        queueRows.getChildren().clear();
        if (upcoming == null || upcoming.isEmpty()) {
            Label empty = new Label("—");
            empty.getStyleClass().add("muted");
            queueRows.getChildren().add(empty);
            return;
        }
        Set<Character> allySet = new HashSet<>();
        if (allies != null) {
            allySet.addAll(allies);
        }
        double now = upcoming.get(0).getActionTime();
        for (int i = 0; i < upcoming.size(); i++) {
            UpcomingTurn turn = upcoming.get(i);
            Character fighter = turn.getCharacter();
            int av = turn.delayFrom(now);
            Label avLabel = new Label(i == 0 ? "NOW" : String.valueOf(av));
            avLabel.getStyleClass().add("queue-av");
            avLabel.setMinWidth(44);
            Label name = new Label(fighter.getName());
            name.getStyleClass().add("queue-name");
            name.setWrapText(true);
            HBox row = new HBox(8, avLabel, name);
            row.getStyleClass().add("queue-row");
            if (i == 0) {
                row.getStyleClass().add("now");
            }
            row.getStyleClass().add(allySet.contains(fighter) ? "ally" : "enemy");
            queueRows.getChildren().add(row);
        }
    }

    void ask(Character actor, List<Move> moves, List<Character> enemies, List<Character> allies,
             CompletableFuture<ActionChoice> future) {
        this.actor = actor;
        refreshCards();
        prompt.setText(actor.getName() + "'s turn — pick a move");
        actions.getChildren().clear();
        for (Move move : actor.getMoves()) {
            boolean ready = containsMove(moves, move);
            Button button = new Button(moveLabel(actor, move));
            button.getStyleClass().add("move-button");
            button.setDisable(!ready);
            if (ready) {
                button.setOnAction(e -> pickMove(actor, move, moves, enemies, allies, future));
            }
            actions.getChildren().add(button);
        }
    }

    void showFinished(String message) {
        actor = null;
        refreshCards();
        prompt.setText(message);
        actions.getChildren().clear();
        home.setVisible(true);
    }

    void cancelPrompt() {
        actions.getChildren().clear();
        prompt.setText("Battle cancelled.");
        home.setVisible(true);
    }

    private void pickMove(Character actor, Move move, List<Move> ready,
                          List<Character> enemies, List<Character> allies,
                          CompletableFuture<ActionChoice> future) {
        List<Character> pool = move.legalTargets(actor, allies, enemies);
        if (move.targetsSelfOnly() || pool.isEmpty()) {
            complete(future, new ActionChoice(move, List.of(actor)));
            return;
        }
        prompt.setText("Choose a target for " + move.getName());
        actions.getChildren().clear();
        for (Character target : pool) {
            Button button = new Button(target.getName()
                    + "  " + target.getStats().getCurrentHp()
                    + "/" + target.getStats().getMaxHp());
            button.getStyleClass().add("move-button");
            button.setOnAction(e -> complete(future, new ActionChoice(move, List.of(target))));
            actions.getChildren().add(button);
        }
        Button back = new Button("Back");
        back.getStyleClass().add("ghost-button");
        back.setOnAction(e -> ask(actor, ready, enemies, allies, future));
        actions.getChildren().add(back);
    }

    private void complete(CompletableFuture<ActionChoice> future, ActionChoice choice) {
        actions.getChildren().clear();
        prompt.setText("Resolving...");
        if (!future.isDone()) {
            future.complete(choice);
        }
    }

    private void rebuild(List<FighterCard> cards, FlowPane box, List<Character> team, boolean enemy) {
        box.getChildren().clear();
        cards.clear();
        if (team == null) {
            return;
        }
        for (Character character : team) {
            FighterCard card = new FighterCard();
            card.bind(character, enemy, actor);
            cards.add(card);
            box.getChildren().add(card);
        }
    }

    private void refreshCards() {
        for (FighterCard card : allyCards) {
            card.refresh(actor);
        }
        for (FighterCard card : enemyCards) {
            card.refresh(actor);
        }
    }

    private static boolean containsMove(List<Move> moves, Move move) {
        for (Move candidate : moves) {
            if (candidate.getName().equals(move.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String moveLabel(Character actor, Move move) {
        int cd = actor.getMoveCooldown(move.getName());
        String extra = cd > 0 ? "  CD " + cd : "";
        return move.getName() + extra;
    }
}
