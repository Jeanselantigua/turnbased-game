package com.battlesim.dungeon;

import com.battlesim.engine.MoveSelector;
import com.battlesim.model.Character;
import com.battlesim.util.RandomProvider;
import java.util.List;

/**
 * Runs a dungeon: same player Characters persist across waves (HP/status carry over).
 * Fill in {@link #run} after EnemyTemplate spawning and Battle wiring feel solid.
 */
public final class DungeonRun {

    private DungeonRun() {
    }

    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random) {
        // TODO:
        // 1. Team player = new Team(party)  — reuse the same instances every wave
        // 2. for each wave index i in dungeon.getWaves():
        //      double scale = dungeon.scaleForWave(i + 1);
        //      Team enemies = wave.spawn(scale);
        //      BattleResult result = Battle.create(player, enemies, playerSelector, enemyAi, random, max, false).run();
        //      if winner is not TEAM_A: return new DungeonResult(i, false, result.getLog());
        // 3. return new DungeonResult(waves.size(), true, combinedLog);
        //
        // Decide later: heal between waves? clear statuses? keep both for a harsher climb.
        return new DungeonResult(0, false, List.of("DungeonRun.run is not implemented yet"));
    }
}
