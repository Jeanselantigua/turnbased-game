package com.battlesim.dungeon;

import com.battlesim.model.Character;
import com.battlesim.model.Inventory;
import java.util.List;

/** Between-wave hub (equip, enhance, leftover move points). Console uses
 * {@link ConsoleCamp}; BalanceSim uses {@link SimCamp}; tests can use {@link #NONE}. */
public interface Camp {

    Camp NONE = (party, inventory, waveNumber, moreWaves) -> { };

    void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves);

    /**
     * Rest or chest every {@link Dungeon#WAYPOINT_EVERY} floors. Default (sims)
     * rests when the party is hurt, otherwise takes the chest.
     */
    default WaypointChoice pickWaypoint(List<Character> party, Inventory inventory, int waveNumber) {
        return Waypoint.autoPick(party);
    }
}
