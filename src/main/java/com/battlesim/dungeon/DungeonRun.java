package com.battlesim.dungeon;

import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.item.DropTable;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.Inventory;
import com.battlesim.model.Move;
import com.battlesim.model.Team;
import com.battlesim.progress.Experience;
import com.battlesim.progress.EvenStatAllocator;
import com.battlesim.progress.StatAllocator;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a dungeon: same player Characters persist across waves (HP/status carry over).
 * Wins grant XP, gold, and gear; level-ups auto-bump HP and speed, then {@link StatAllocator}
 * spends leftover points. Console play can open a {@link Camp} between waves.
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
        return run(party, dungeon, playerSelector, random, keepLog, verbose,
                new EvenStatAllocator());
    }

    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random,
                                    boolean keepLog,
                                    boolean verbose,
                                    StatAllocator allocator) {
        return run(party, dungeon, playerSelector, random, keepLog, verbose,
                allocator, new Inventory(), Camp.NONE);
    }

    public static DungeonResult run(List<Character> party,
                                    Dungeon dungeon,
                                    MoveSelector playerSelector,
                                    RandomProvider random,
                                    boolean keepLog,
                                    boolean verbose,
                                    StatAllocator allocator,
                                    Inventory inventory,
                                    Camp camp) {
        Team player = new Team(party);
        MoveSelector enemyAi = new SimpleAiMoveSelector(random);
        List<String> combinedLog = keepLog ? new ArrayList<>() : List.of();
        List<Wave> waves = dungeon.getWaves();
        StatAllocator points = allocator != null ? allocator : new EvenStatAllocator();
        Inventory bag = inventory != null ? inventory : new Inventory();
        Camp hub = camp != null ? camp : Camp.NONE;
        // Separate from combat RNG so drops do not shift BalanceSim fight sequences.
        RandomProvider lootRng = new RandomProvider();

        for (int i = 0; i < waves.size(); i++) {
            int waveNumber = i + 1;
            Wave wave = waves.get(i);
            double scale = dungeon.scaleForWave(waveNumber, party.size());
            Team enemies = wave.spawn(scale);
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

            awardWaveXp(party, wave, scale, points, keepLog, combinedLog, verbose);
            awardLoot(wave, scale, bag, lootRng, keepLog, combinedLog, verbose);
            hub.afterWave(party, bag, waveNumber, waveNumber < waves.size());
        }
        return new DungeonResult(waves.size(), true, combinedLog);
    }

    private static void awardLoot(Wave wave,
                                  double scale,
                                  Inventory inventory,
                                  RandomProvider random,
                                  boolean keepLog,
                                  List<String> combinedLog,
                                  boolean verbose) {
        DropTable.Loot loot = DropTable.roll(wave, scale, random);
        if (loot.getGold() > 0) {
            inventory.addGold(loot.getGold());
            String goldLine = "Gained " + loot.getGold() + " gold (total " + inventory.getGold() + ")";
            record(goldLine, keepLog, combinedLog, verbose);
        }
        for (Gear drop : loot.getDrops()) {
            inventory.add(drop);
            record("Dropped " + drop.describe(), keepLog, combinedLog, verbose);
        }
    }

    private static void record(String line, boolean keepLog, List<String> combinedLog, boolean verbose) {
        if (keepLog) {
            combinedLog.add(line);
        }
        if (verbose) {
            System.out.println(line);
        }
    }

    private static void awardWaveXp(List<Character> party,
                                    Wave wave,
                                    double scale,
                                    StatAllocator allocator,
                                    boolean keepLog,
                                    List<String> combinedLog,
                                    boolean verbose) {
        int xpGain = Experience.forWave(wave, scale);
        if (xpGain <= 0) {
            return;
        }
        for (Character member : party) {
            int levels = member.grantXp(xpGain);
            String line = xpLine(member, xpGain, levels);
            if (keepLog) {
                combinedLog.add(line);
            }
            if (verbose) {
                System.out.println(line);
            }
            for (Move learned : member.getLastUnlockedMoves()) {
                String learnedLine = member.getName() + " learned " + learned.getName() + "!";
                if (keepLog) {
                    combinedLog.add(learnedLine);
                }
                if (verbose) {
                    System.out.println(learnedLine);
                }
            }
            allocator.allocate(member);
        }
    }

    private static String xpLine(Character member, int xpGain, int levels) {
        if (levels > 0) {
            return member.getName() + " gained " + xpGain + " XP and reached level "
                    + member.getLevel() + " (" + member.getXp() + "/"
                    + member.getXpToNextLevel() + ")";
        }
        return member.getName() + " gained " + xpGain + " XP (Lv " + member.getLevel()
                + " " + member.getXp() + "/" + member.getXpToNextLevel() + ")";
    }

    private static void printHp(String title, List<Character> members) {
        System.out.print(title + ":");
        for (Character member : members) {
            System.out.print("  " + member.getName() + " Lv" + member.getLevel() + " "
                    + member.getStats().getCurrentHp() + "/" + member.getStats().getMaxHp());
        }
        System.out.println();
    }
}
