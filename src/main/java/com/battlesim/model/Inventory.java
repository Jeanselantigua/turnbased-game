package com.battlesim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Party stash: unequipped gear plus gold for enhancements. */
public final class Inventory {

    private final List<Gear> pieces = new ArrayList<>();
    private int gold;

    public List<Gear> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public int size() {
        return pieces.size();
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    public Gear get(int index) {
        return pieces.get(index);
    }

    public void add(Gear gear) {
        if (gear != null) {
            pieces.add(gear);
        }
    }

    public boolean remove(Gear gear) {
        return gear != null && pieces.remove(gear);
    }

    public boolean contains(Gear gear) {
        return gear != null && pieces.contains(gear);
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        if (amount > 0) {
            gold += amount;
        }
    }

    public boolean trySpendGold(int amount) {
        if (amount <= 0 || gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }
}
