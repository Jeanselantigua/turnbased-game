package com.battlesim.ui;

import com.battlesim.dungeon.Dungeon;
import com.battlesim.dungeon.DungeonResult;
import com.battlesim.dungeon.DungeonRun;
import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleObserver;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.Inventory;
import com.battlesim.model.Team;
import com.battlesim.progress.BankStatAllocator;
import com.battlesim.util.RandomProvider;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameApp extends Application {

    private static final int TEAM_SLOTS = 3;

    private final StackPane root = new StackPane();
    private final BattleView battleView = new BattleView();
    private final FxMoveSelector playerSelector = new FxMoveSelector(battleView);
    private FxCamp camp;
    private Thread fightThread;

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(root, 1920, 1080);
        var css = GameApp.class.getResource("/com/battlesim/ui/game.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        root.getStyleClass().add("root-pane");
        battleView.setOnHome(this::showTitle);
        stage.setTitle("RPG Battle Sim");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> stopFight());
        showTitle();
        stage.show();
    }

    @Override
    public void stop() {
        stopFight();
    }

    private void showTitle() {
        stopFight();
        Label title = new Label("RPG Battle Sim");
        title.getStyleClass().add("hero-title");
        Label subtitle = new Label("JavaFX shell on the existing battle engine. Console Main.java still works.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        Button pvp = new Button("PvP vs AI");
        pvp.getStyleClass().add("primary-button");
        pvp.setOnAction(e -> pickPlayerTeam(false));

        Button dungeon = new Button("Dungeon climb");
        dungeon.getStyleClass().add("primary-button");
        dungeon.setOnAction(e -> pickPlayerTeam(true));

        Label note = new Label("Dungeon camp: equip, upgrade, sell, spend leftover points. Skill tree still later.");
        note.getStyleClass().add("muted");
        note.setWrapText(true);

        VBox box = new VBox(16, title, subtitle, pvp, dungeon, note);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("screen");
        show(box);
    }

    private void pickPlayerTeam(boolean dungeon) {
        String heading = dungeon ? "Pick your dungeon party" : "Pick your team";
        show(new TeamPickView(heading, TEAM_SLOTS, !dungeon, this::showTitle, team -> {
            if (dungeon) {
                startDungeon(team);
            } else {
                pickAiTeam(team);
            }
        }));
    }

    private void pickAiTeam(List<Character> playerTeam) {
        show(new TeamPickView("Pick the AI team", TEAM_SLOTS, true, () -> pickPlayerTeam(false),
                aiTeam -> startPvp(playerTeam, aiTeam)));
    }

    private void startPvp(List<Character> playerTeam, List<Character> aiTeam) {
        stopFight();
        battleView.reset();
        battleView.setBanner("PvP");
        battleView.showField(playerTeam, aiTeam);
        show(battleView);

        RandomProvider random = new RandomProvider();
        BattleObserver observer = new FxBattleObserver(battleView);
        runFight(() -> {
            BattleResult result = Battle.create(
                    new Team(playerTeam), new Team(aiTeam),
                    playerSelector, new SimpleAiMoveSelector(random),
                    random, Battle.DEFAULT_MAX_ACTIONS, false)
                    .withObserver(observer)
                    .run();
            Platform.runLater(() -> battleView.showFinished(pvpMessage(result)));
        });
    }

    private void startDungeon(List<Character> party) {
        stopFight();
        battleView.reset();
        battleView.setBanner("Dungeon");
        battleView.showField(party, List.of());
        show(battleView);

        RandomProvider random = new RandomProvider();
        BattleObserver observer = new FxBattleObserver(battleView);
        FxCamp hub = new FxCamp(this::show, battleView, random);
        camp = hub;
        runFight(() -> {
            DungeonResult result = DungeonRun.run(
                    party, Dungeon.standard(random), playerSelector, random,
                    false, false, new BankStatAllocator(), new Inventory(),
                    hub, observer);
            Platform.runLater(() -> battleView.showFinished(dungeonMessage(result)));
        });
    }

    private void runFight(Runnable fight) {
        fightThread = new Thread(() -> {
            try {
                fight.run();
            } catch (RuntimeException ex) {
                Platform.runLater(() -> battleView.showFinished("Battle stopped."));
            }
        }, "battle");
        fightThread.setDaemon(true);
        fightThread.start();
    }

    private void stopFight() {
        playerSelector.cancel();
        if (camp != null) {
            camp.cancel();
            camp = null;
        }
        if (fightThread != null) {
            fightThread.interrupt();
            fightThread = null;
        }
    }

    private void show(Node node) {
        root.getChildren().setAll(node);
    }

    private static String pvpMessage(BattleResult result) {
        switch (result.getWinner()) {
            case TEAM_A:
                return "You win!";
            case TEAM_B:
                return "Defeat.";
            case TIMEOUT:
                return "Battle timed out.";
            default:
                return "Draw — both teams fell.";
        }
    }

    private static String dungeonMessage(DungeonResult result) {
        if (result.clearedAll()) {
            return "Cleared all " + result.getWavesCleared() + " floors!";
        }
        return "Wiped on floor " + (result.getWavesCleared() + 1)
                + "  (" + result.getWavesCleared() + " cleared).";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
