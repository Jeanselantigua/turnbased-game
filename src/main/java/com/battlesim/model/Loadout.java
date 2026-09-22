package com.battlesim.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** The six equipped pieces on one character. */
public final class Loadout {

    private final EnumMap<GearSlot, Gear> pieces = new EnumMap<>(GearSlot.class);
    private final EnumMap<StatKind, Integer> twoPieceBonuses = new EnumMap<>(StatKind.class);

    public Gear get(GearSlot slot) {
        return pieces.get(slot);
    }

    public boolean has(Gear gear) {
        return gear != null && pieces.get(gear.getSlot()) == gear;
    }

    public Map<GearSlot, Gear> all() {
        return Collections.unmodifiableMap(pieces);
    }

    /**
     * Moves {@code gear} out of the bag onto this wearer. Occupied slots
     * swap the old piece back into the bag.
     */
    public boolean equip(Character wearer, Gear gear, Inventory inventory) {
        if (wearer == null || gear == null || inventory == null) {
            return false;
        }
        if (has(gear)) {
            return true;
        }
        if (!inventory.remove(gear)) {
            return false;
        }
        Gear previous = pieces.put(gear.getSlot(), gear);
        if (previous != null) {
            wearer.applyGearBonuses(previous, -1);
            inventory.add(previous);
        }
        wearer.applyGearBonuses(gear, 1);
        return true;
    }

    public Gear unequip(Character wearer, GearSlot slot, Inventory inventory) {
        if (wearer == null || slot == null || inventory == null) {
            return null;
        }
        Gear previous = pieces.remove(slot);
        if (previous == null) {
            return null;
        }
        wearer.applyGearBonuses(previous, -1);
        inventory.add(previous);
        return previous;
    }

    public int count(GearSet set) {
        int n = 0;
        for (Gear piece : pieces.values()) {
            if (piece.getSet() == set) {
                n++;
            }
        }
        return n;
    }

    public int twoPieceBonus(StatKind kind) {
        return twoPieceBonuses.getOrDefault(kind, 0);
    }

    public void clearTwoPieceBonuses(Character wearer) {
        if (wearer == null) {
            twoPieceBonuses.clear();
            return;
        }
        for (Map.Entry<StatKind, Integer> entry : twoPieceBonuses.entrySet()) {
            int amount = entry.getValue();
            if (amount != 0) {
                wearer.getStats().add(entry.getKey(), -amount);
            }
        }
        twoPieceBonuses.clear();
    }

    public void addTwoPieceBonus(Character wearer, StatKind kind, int amount) {
        if (wearer == null || kind == null || amount == 0) {
            return;
        }
        wearer.getStats().add(kind, amount);
        twoPieceBonuses.put(kind, twoPieceBonuses.getOrDefault(kind, 0) + amount);
    }
}
