package com.battlesim.dungeon;

import com.battlesim.item.SimGearAllocator;
import com.battlesim.model.Character;
import com.battlesim.model.Inventory;
import com.battlesim.util.RandomProvider;
import java.util.List;

/**
 * BalanceSim camp: equip from the bag (set bonuses first), then upgrade.
 * Waypoints still use {@link Waypoint#autoPick}.
 */
public final class SimCamp implements Camp {

    private final RandomProvider random;

    public SimCamp() {
        this(new RandomProvider());
    }

    public SimCamp(RandomProvider random) {
        this.random = random == null ? new RandomProvider() : random;
    }

    @Override
    public void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves) {
        SimGearAllocator.camp(party, inventory, random);
    }
}
