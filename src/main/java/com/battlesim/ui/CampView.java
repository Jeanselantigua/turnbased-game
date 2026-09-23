package com.battlesim.ui;

import com.battlesim.item.SetBonuses;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearSlot;
import com.battlesim.model.Inventory;
import com.battlesim.model.Move;
import com.battlesim.model.StatKind;
import com.battlesim.model.Stats;
import com.battlesim.progress.Growth;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Rough camp: party sheet, equip / unequip / upgrade / sell, leftover points. */
final class CampView extends BorderPane {

    private final List<Character> party;
    private final Inventory inventory;
    private final RandomProvider random;
    private final Runnable onContinue;
    private final Label gold = new Label();
    private final Label message = new Label();
    private final VBox partyBox = new VBox(6);
    private final VBox sheetBox = new VBox(8);
    private final VBox bagBox = new VBox(6);
    private Character selected;
    private Gear selectedBag;
    private GearSlot selectedSlot;

    CampView(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves,
             RandomProvider random, Runnable onContinue) {
        this.party = party;
        this.inventory = inventory;
        this.random = random;
        this.onContinue = onContinue;
        getStyleClass().add("screen");
        setPadding(new Insets(16));

        Label heading = new Label("Camp — floor " + waveNumber + " cleared");
        heading.getStyleClass().add("title");
        gold.getStyleClass().add("camp-gold");
        message.getStyleClass().add("prompt");
        message.setWrapText(true);
        message.setText(moreWaves ? "Equip, upgrade, or spend leftover points." : "Last floor. Bank leftover gold and gear.");

        Button cont = new Button(moreWaves ? "Continue" : "Finish");
        cont.getStyleClass().add("primary-button");
        cont.setOnAction(e -> onContinue.run());

        VBox top = new VBox(8, heading, gold, message);
        setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 12, 0));

        Label partyLabel = new Label("Party");
        partyLabel.getStyleClass().add("section-label");
        ScrollPane partyPane = wrap(partyBox);
        VBox left = new VBox(8, partyLabel, partyPane);
        left.setPrefWidth(220);
        VBox.setVgrow(partyPane, Priority.ALWAYS);
        setLeft(left);

        Label bagLabel = new Label("Bag");
        bagLabel.getStyleClass().add("section-label");
        ScrollPane bagPane = wrap(bagBox);
        VBox right = new VBox(8, bagLabel, bagPane);
        right.setPrefWidth(320);
        VBox.setVgrow(bagPane, Priority.ALWAYS);
        setRight(right);
        BorderPane.setMargin(left, new Insets(0, 12, 0, 0));
        BorderPane.setMargin(right, new Insets(0, 0, 0, 12));

        ScrollPane sheetPane = wrap(sheetBox);
        VBox center = new VBox(8, sheetPane, cont);
        VBox.setVgrow(sheetPane, Priority.ALWAYS);
        setCenter(center);

        selected = firstMember();
        refresh();
    }

    private Character firstMember() {
        for (Character member : party) {
            if (member != null && !member.isSummon()) {
                return member;
            }
        }
        return null;
    }

    private void refresh() {
        gold.setText("Gold: " + inventory.getGold() + "    Bag: " + inventory.size());
        rebuildParty();
        rebuildSheet();
        rebuildBag();
    }

    private void rebuildParty() {
        partyBox.getChildren().clear();
        for (Character member : party) {
            if (member == null || member.isSummon()) {
                continue;
            }
            String extra = "";
            if (member.getUnspentStatPoints() > 0 || member.getUnspentMovePoints() > 0) {
                extra = "  +" + member.getUnspentStatPoints() + " stat  +"
                        + member.getUnspentMovePoints() + " move";
            }
            Button button = new Button(member.getName() + "  Lv " + member.getLevel()
                    + "  " + member.getStats().getCurrentHp() + "/" + member.getStats().getMaxHp()
                    + extra);
            button.getStyleClass().add("roster-card");
            if (member == selected) {
                button.getStyleClass().add("selected");
            }
            button.setMaxWidth(Double.MAX_VALUE);
            button.setWrapText(true);
            button.setOnAction(e -> {
                selected = member;
                selectedSlot = null;
                refresh();
            });
            partyBox.getChildren().add(button);
        }
    }

    private void rebuildSheet() {
        sheetBox.getChildren().clear();
        if (selected == null) {
            sheetBox.getChildren().add(muted("Pick a party member."));
            return;
        }
        Stats stats = selected.getStats();
        Label name = new Label(selected.getName() + "  Lv " + selected.getLevel()
                + "  XP " + selected.getXp() + "/" + selected.getXpToNextLevel());
        name.getStyleClass().add("banner");
        Label line = new Label("HP " + stats.getCurrentHp() + "/" + stats.getMaxHp()
                + "  ATK " + stats.getAttack()
                + "  DEF " + stats.getDefense()
                + "  MATK " + stats.getMagicAttack()
                + "  MDEF " + stats.getMagicDefense()
                + "  SPD " + stats.getSpeed()
                + "  CRIT " + selected.getCritRate() + "%"
                + "  CDMG " + selected.getCritDamage() + "%");
        line.getStyleClass().add("camp-sheet");
        line.setWrapText(true);
        sheetBox.getChildren().addAll(name, line);
        String sets = SetBonuses.summary(selected.getLoadout());
        if (!sets.isEmpty()) {
            Label setLine = new Label("Sets: " + sets);
            setLine.getStyleClass().add("muted");
            setLine.setWrapText(true);
            sheetBox.getChildren().add(setLine);
        }

        for (GearSlot slot : GearSlot.values()) {
            Gear piece = selected.getLoadout().get(slot);
            String text = slot.getLabel() + ": " + (piece == null ? "(empty)" : piece.describe());
            Button button = new Button(text);
            button.getStyleClass().add("slot-button");
            if (selectedSlot == slot) {
                button.getStyleClass().add("selected");
            }
            button.setMaxWidth(Double.MAX_VALUE);
            button.setWrapText(true);
            button.setOnAction(e -> {
                selectedSlot = slot;
                selectedBag = null;
                refresh();
            });
            sheetBox.getChildren().add(button);
        }

        HBox gearActions = new HBox(8);
        gearActions.getChildren().add(action("Equip from bag", this::equipSelected));
        gearActions.getChildren().add(action("Unequip", this::unequipSelected));
        gearActions.getChildren().add(action("Upgrade", this::upgradeSelected));
        gearActions.getChildren().add(action("Sell", this::sellSelected));
        sheetBox.getChildren().add(gearActions);

        if (selected.getUnspentStatPoints() > 0) {
            Label spend = new Label("Stat points: " + selected.getUnspentStatPoints());
            spend.getStyleClass().add("section-label");
            FlowPane statsBox = new FlowPane(8, 8);
            for (StatKind kind : StatKind.pointStats()) {
                boolean specialty = selected.getSpecialties().contains(kind);
                Button button = new Button(kind.getLabel() + " +" + Growth.pointGain(kind, specialty)
                        + (specialty ? " *" : ""));
                button.getStyleClass().add("move-button");
                button.setOnAction(e -> {
                    int gain = selected.spendStatPoint(kind);
                    message.setText(gain > 0
                            ? selected.getName() + " +" + gain + " " + kind.getLabel()
                            : "No stat points left.");
                    refresh();
                });
                statsBox.getChildren().add(button);
            }
            sheetBox.getChildren().addAll(spend, statsBox);
        }
        if (selected.getUnspentMovePoints() > 0) {
            Label spend = new Label("Move points: " + selected.getUnspentMovePoints());
            spend.getStyleClass().add("section-label");
            FlowPane movesBox = new FlowPane(8, 8);
            for (Move move : selected.getMoves()) {
                Button button = new Button(move.getName() + "  rank " + selected.getMoveRank(move));
                button.getStyleClass().add("move-button");
                button.setOnAction(e -> {
                    int rank = selected.spendMovePoint(move);
                    message.setText(rank >= 0
                            ? selected.getName() + " ranked " + move.getName() + " to " + rank
                            : "No move points left.");
                    refresh();
                });
                movesBox.getChildren().add(button);
            }
            sheetBox.getChildren().addAll(spend, movesBox);
        }
    }

    private void rebuildBag() {
        bagBox.getChildren().clear();
        if (inventory.isEmpty()) {
            bagBox.getChildren().add(muted("Bag is empty."));
            return;
        }
        List<Gear> pieces = new ArrayList<>(inventory.getPieces());
        for (Gear piece : pieces) {
            int cost = Gear.upgradeCost(piece.getLevel());
            String extra = cost > 0 ? "  (" + cost + "g to upgrade, " + piece.sellValue() + "g sell)"
                    : "  (max, " + piece.sellValue() + "g sell)";
            Button button = new Button(piece.describe() + extra);
            button.getStyleClass().add("bag-item");
            if (piece == selectedBag) {
                button.getStyleClass().add("selected");
            }
            button.setMaxWidth(Double.MAX_VALUE);
            button.setWrapText(true);
            button.setOnAction(e -> {
                selectedBag = piece;
                selectedSlot = null;
                refresh();
            });
            bagBox.getChildren().add(button);
        }
    }

    private void equipSelected() {
        if (selected == null) {
            message.setText("Pick a character first.");
            return;
        }
        if (selectedBag == null) {
            message.setText("Pick a bag piece, then Equip.");
            return;
        }
        Gear piece = selectedBag;
        if (selected.equip(piece, inventory)) {
            message.setText("Equipped " + piece.summary());
            selectedBag = null;
        } else {
            message.setText("Could not equip that piece.");
        }
        refresh();
    }

    private void unequipSelected() {
        if (selected == null || selectedSlot == null) {
            message.setText("Pick an equipped slot to unequip.");
            return;
        }
        Gear removed = selected.unequip(selectedSlot, inventory);
        if (removed == null) {
            message.setText("That slot is empty.");
        } else {
            message.setText("Bagged " + removed.summary());
            selectedBag = removed;
            selectedSlot = null;
        }
        refresh();
    }

    private void upgradeSelected() {
        Gear piece = selectedPiece();
        if (piece == null || selected == null) {
            message.setText("Pick a bag piece or an equipped slot to upgrade.");
            return;
        }
        if (piece.getLevel() >= Gear.MAX_LEVEL) {
            message.setText("Already +" + Gear.MAX_LEVEL + ".");
            return;
        }
        int cost = Gear.upgradeCost(piece.getLevel());
        if (inventory.getGold() < cost) {
            message.setText("Need " + cost + " gold.");
            return;
        }
        if (!selected.upgradeGear(piece, inventory, random)) {
            message.setText("Could not upgrade.");
            return;
        }
        message.setText(piece.describe());
        refresh();
    }

    private void sellSelected() {
        if (selectedBag != null) {
            Gear piece = selectedBag;
            int value = inventory.sell(piece);
            selectedBag = null;
            message.setText(value > 0 ? "Sold for " + value + " gold." : "Could not sell that.");
            refresh();
            return;
        }
        if (selected == null || selectedSlot == null) {
            message.setText("Pick a bag piece or an equipped slot to sell.");
            return;
        }
        Gear piece = selected.getLoadout().get(selectedSlot);
        if (piece == null) {
            message.setText("That slot is empty.");
            return;
        }
        int value = selected.sellEquipped(selectedSlot, inventory);
        selectedSlot = null;
        message.setText("Sold for " + value + " gold.");
        refresh();
    }

    private Gear selectedPiece() {
        if (selectedBag != null) {
            return selectedBag;
        }
        if (selected != null && selectedSlot != null) {
            return selected.getLoadout().get(selectedSlot);
        }
        return null;
    }

    private Button action(String label, Runnable run) {
        Button button = new Button(label);
        button.getStyleClass().add("ghost-button");
        button.setOnAction(e -> run.run());
        return button;
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        return label;
    }

    private static ScrollPane wrap(VBox box) {
        ScrollPane pane = new ScrollPane(box);
        pane.setFitToWidth(true);
        pane.getStyleClass().add("log-pane");
        return pane;
    }
}
