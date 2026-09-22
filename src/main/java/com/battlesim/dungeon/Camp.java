package com.battlesim.dungeon;

import com.battlesim.model.Character;
import com.battlesim.model.Inventory;
import java.util.List;

/** Between-wave hub (equip, enhance, leftover move points). Sims use {@link #NONE}. */
public interface Camp {

    Camp NONE = (party, inventory, waveNumber, moreWaves) -> { };

    void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves);
}
