package com.battlesim.dungeon;

import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.Team;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a dungeon: same player Characters persist across waves (HP/status carry over).
 */
public final class DungeonRun {

    private DungeonRun() {
    }

    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random) {
        return run(party, dungeon, playerSelector, random, true);
    }

    /**
     * @param keepLog false for batch sims so wave transcripts are not retained
     */
    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random,
                                    boolean keepLog) {
        return run(party, dungeon, playerSelector, random, keepLog, false);
    }

    /**
     * @param verbose true to print each wave and live combat (console play)
     */
    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random,
                                    boolean keepLog,
                                    boolean verbose) {
        Team player = new Team(party);
        MoveSelector enemyAi = new SimpleAiMoveSelector(random);
        List<String> combinedLog = keepLog ? new ArrayList<>() : List.of();
        List<Wave> waves = dungeon.getWaves();

        for (int i = 0; i < waves.size(); i++) {
            int waveNumber = i + 1;
            double scale = dungeon.scaleForWave(waveNumber, party.size());
            Team enemies = waves.get(i).spawn(scale);
            String label = (Dungeon.isBossWave(waveNumber) ? "Boss Wave " : "Wave ") + waveNumber;
            if (keepLog) {
                combinedLog.add("--- " + label + " ---");
            }
            if (verbose) {
                System.out.println();
                System.out.println("--- " + label + " ---");
                printHp("Party", party);
            }

            BattleResult result = Battle.create(
                    player, enemies, playerSelector, enemyAi,
                    random, Battle.DEFAULT_MAX_ACTIONS, verbose).run();
            if (keepLog) {
                combinedLog.addAll(result.getLog());
            }

            if (result.getWinner() != BattleResult.Winner.TEAM_A) {
                return new DungeonResult(i, false, combinedLog);
            }
        }
        return new DungeonResult(waves.size(), true, combinedLog);
    }

    private static void printHp(String title, List<Character> members) {
        System.out.print(title + ":");
        for (Character member : members) {
            System.out.print("  " + member.getName() + " "
                    + member.getStats().getCurrentHp() + "/" + member.getStats().getMaxHp());
        }
        System.out.println();
    }
}
