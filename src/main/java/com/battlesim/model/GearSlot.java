package com.battlesim.model;

import java.util.List;

/**
 * Six equipment slots. Main-stat pools are slot-locked the way disc-drive
 * slots are in Zenless Zone Zero; set bonuses come later.
 */
public enum GearSlot {
    HELM("Helm", List.of(StatKind.HP)),
    GLOVES("Gloves", List.of(StatKind.ATTACK)),
    CHEST("Chest", List.of(StatKind.DEFENSE)),
    BOOTS("Boots", List.of(StatKind.SPEED)),
    AMULET("Amulet", List.of(StatKind.MAGIC_ATTACK, StatKind.MAGIC_DEFENSE)),
    RING("Ring", List.of(StatKind.HP, StatKind.ATTACK, StatKind.DEFENSE,
            StatKind.MAGIC_ATTACK, StatKind.MAGIC_DEFENSE, StatKind.SPEED));

    private final String label;
    private final List<StatKind> mainStatPool;

    GearSlot(String label, List<StatKind> mainStatPool) {
        this.label = label;
        this.mainStatPool = List.copyOf(mainStatPool);
    }

    public String getLabel() {
        return label;
    }

    public List<StatKind> getMainStatPool() {
        return mainStatPool;
    }
}
