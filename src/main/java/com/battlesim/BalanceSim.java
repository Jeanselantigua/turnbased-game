package com.battlesim;

import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.Battle;
import com.battlesim.engine.BattleResult;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Team;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch-fights the roster (1v1 and 4v4) with a simple AI and prints
 * win-rate / survival tables. Run as a Java Application from Eclipse.
 */
public class BalanceSim {

    static final int FIGHTS_PER_1V1 = 500;
    static final int FIGHTS_PER_SPLIT = 50;
    static final int MIXED_4V4_FIGHTS = 500;
    static final int MAX_ACTIONS = 200;
    static final int TEAM_SIZE = 4;

    public static void main(String[] args) {
        List<CharacterTemplate> roster = PlayableCharacters.all();
        RandomProvider random = new RandomProvider();

        System.out.println("=== Battle Balance Simulator ===");
        System.out.println("Roster: " + joinNames(roster));
        System.out.println("AI: SimpleAiMoveSelector  |  max actions: " + MAX_ACTIONS);
        System.out.println();

        run1v1(roster, random);
        runUniqueSplits(roster, random);
        runMixedComps(roster, random);
        // TODO Phase 2: runDungeonProgress(roster, random);
    }

    static BattleResult fight(List<CharacterTemplate> teamATemplates,
                              List<CharacterTemplate> teamBTemplates,
                              RandomProvider random) {
        List<Character> teamAMembers = spawn(teamATemplates);
        List<Character> teamBMembers = spawn(teamBTemplates);
        Team teamA = new Team(teamAMembers);
        Team teamB = new Team(teamBMembers);
        MoveSelector ai = new SimpleAiMoveSelector(random);
        return Battle.create(teamA, teamB, ai, ai, random, MAX_ACTIONS, false).run();
    }

    private static List<Character> spawn(List<CharacterTemplate> templates) {
        List<Character> members = new ArrayList<>();
        for (CharacterTemplate template : templates) {
            members.add(template.createInstance());
        }
        return members;
    }

    private static void run1v1(List<CharacterTemplate> roster, RandomProvider random) {
        int n = roster.size();
        int[][] wins = new int[n][n];
        int draws = 0;
        int timeouts = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                List<CharacterTemplate> a = List.of(roster.get(i));
                List<CharacterTemplate> b = List.of(roster.get(j));
                for (int k = 0; k < FIGHTS_PER_1V1; k++) {
                    BattleResult result = fight(a, b, random);
                    switch (result.getWinner()) {
                        case TEAM_A:
                            wins[i][j]++;
                            break;
                        case TEAM_B:
                            break;
                        case DRAW:
                            draws++;
                            break;
                        case TIMEOUT:
                            timeouts++;
                            break;
                    }
                }
            }
        }

        System.out.println("1v1 win% (row vs column), " + FIGHTS_PER_1V1 + " fights each");
        printMatchupMatrix(roster, wins, FIGHTS_PER_1V1);
        System.out.println("Draws: " + draws + "   Timeouts: " + timeouts);
        System.out.println();
        System.out.println("1v1 overall ranking (avg win% as Team A)");
        print1v1Ranking(roster, wins, FIGHTS_PER_1V1);
        System.out.println();
    }

    private static void printMatchupMatrix(List<CharacterTemplate> roster, int[][] wins, int fights) {
        int col = 14;
        System.out.printf("%-" + col + "s", "");
        for (CharacterTemplate template : roster) {
            System.out.printf("%" + col + "s", truncate(template.getName(), col - 1));
        }
        System.out.println();

        for (int i = 0; i < roster.size(); i++) {
            System.out.printf("%-" + col + "s", truncate(roster.get(i).getName(), col - 1));
            for (int j = 0; j < roster.size(); j++) {
                if (i == j) {
                    System.out.printf("%" + col + "s", "--");
                } else {
                    double pct = (wins[i][j] * 100.0) / fights;
                    System.out.printf("%" + col + "s", String.format("%.0f%%", pct));
                }
            }
            System.out.println();
        }
    }

    private static void print1v1Ranking(List<CharacterTemplate> roster, int[][] wins, int fights) {
        int opponents = roster.size() - 1;
        List<RankRow> rows = new ArrayList<>();
        for (int i = 0; i < roster.size(); i++) {
            int totalWins = 0;
            for (int j = 0; j < roster.size(); j++) {
                if (i != j) {
                    totalWins += wins[i][j];
                }
            }
            double pct = (totalWins * 100.0) / (fights * opponents);
            rows.add(new RankRow(roster.get(i).getName(), pct, totalWins, fights * opponents));
        }
        rows.sort(Comparator.comparingDouble((RankRow r) -> r.pct).reversed());
        for (int i = 0; i < rows.size(); i++) {
            RankRow row = rows.get(i);
            System.out.printf("  %d. %-14s  %5.1f%%  (%d/%d)%n",
                    i + 1, row.name, row.pct, row.wins, row.fights);
        }
    }

    private static void runUniqueSplits(List<CharacterTemplate> roster, RandomProvider random) {
        int poolSize = TEAM_SIZE * 2;
        System.out.println("Unique 4v4 splits (each character used once), "
                + FIGHTS_PER_SPLIT + " fights each");

        if (roster.size() < poolSize) {
            System.out.println("  skipped (need at least " + poolSize + " characters)");
            System.out.println();
            return;
        }

        int totalDraws = 0;
        int totalTimeouts = 0;
        int printed = 0;
        for (int[] pool : combinations(roster.size(), poolSize)) {
            List<CharacterTemplate> subRoster = new ArrayList<>();
            for (int idx : pool) {
                subRoster.add(roster.get(idx));
            }
            int[] totals = runUniqueSplitsForPool(subRoster, random);
            printed += totals[0];
            totalDraws += totals[1];
            totalTimeouts += totals[2];
        }

        System.out.println("Splits reported: " + printed
                + "   Draws: " + totalDraws + "   Timeouts: " + totalTimeouts);
        System.out.println();
    }

    /** Returns [printed, draws, timeouts] for one 6-character pool. */
    private static int[] runUniqueSplitsForPool(List<CharacterTemplate> roster, RandomProvider random) {
        int printed = 0;
        int totalDraws = 0;
        int totalTimeouts = 0;
        List<int[]> combos = combinations(roster.size(), TEAM_SIZE);

        for (int[] combo : combos) {
            boolean[] inA = new boolean[roster.size()];
            for (int idx : combo) {
                inA[idx] = true;
            }
            List<CharacterTemplate> teamA = new ArrayList<>();
            List<CharacterTemplate> teamB = new ArrayList<>();
            for (int i = 0; i < roster.size(); i++) {
                if (inA[i]) {
                    teamA.add(roster.get(i));
                } else {
                    teamB.add(roster.get(i));
                }
            }
            if (teamA.size() != TEAM_SIZE || teamB.size() != TEAM_SIZE) {
                continue;
            }

            String keyA = joinNames(teamA);
            String keyB = joinNames(teamB);
            if (keyA.compareTo(keyB) >= 0) {
                continue;
            }

            int aWins = 0;
            int bWins = 0;
            int draws = 0;
            int timeouts = 0;
            for (int k = 0; k < FIGHTS_PER_SPLIT; k++) {
                BattleResult result = fight(teamA, teamB, random);
                switch (result.getWinner()) {
                    case TEAM_A:
                        aWins++;
                        break;
                    case TEAM_B:
                        bWins++;
                        break;
                    case DRAW:
                        draws++;
                        break;
                    case TIMEOUT:
                        timeouts++;
                        break;
                }
            }
            totalDraws += draws;
            totalTimeouts += timeouts;
            printed++;

            System.out.printf("  %s  vs  %s%n", keyA, keyB);
            System.out.printf("      A: %5.1f%%   B: %5.1f%%   draw: %5.1f%%   timeout: %5.1f%%%n",
                    pct(aWins, FIGHTS_PER_SPLIT),
                    pct(bWins, FIGHTS_PER_SPLIT),
                    pct(draws, FIGHTS_PER_SPLIT),
                    pct(timeouts, FIGHTS_PER_SPLIT));
        }
        return new int[] {printed, totalDraws, totalTimeouts};
    }

    private static void runMixedComps(List<CharacterTemplate> roster, RandomProvider random) {
        System.out.println("Mixed 4v4 (templates can repeat), " + MIXED_4V4_FIGHTS + " fights");

        Map<String, CharacterAgg> stats = new LinkedHashMap<>();
        for (CharacterTemplate template : roster) {
            stats.put(template.getName(), new CharacterAgg(template.getName()));
        }

        int draws = 0;
        int timeouts = 0;
        for (int i = 0; i < MIXED_4V4_FIGHTS; i++) {
            List<CharacterTemplate> teamA = randomTeam(roster, random);
            List<CharacterTemplate> teamB = randomTeam(roster, random);
            BattleResult result = fight(teamA, teamB, random);
            switch (result.getWinner()) {
                case DRAW:
                    draws++;
                    break;
                case TIMEOUT:
                    timeouts++;
                    break;
                default:
                    break;
            }
            recordMixed(stats, result);
        }

        List<CharacterAgg> rows = new ArrayList<>(stats.values());
        rows.sort(Comparator.comparingDouble((CharacterAgg a) -> a.winRate()).reversed());

        System.out.printf("  %-14s %8s %10s %12s %10s%n",
                "Character", "Win%", "Survive%", "HP% if win", "Present");
        for (CharacterAgg row : rows) {
            System.out.printf("  %-14s %7.1f%% %9.1f%% %11.1f%% %10d%n",
                    row.name, row.winRate(), row.surviveRate(), row.avgHpWhenTeamWon(), row.appearances);
        }
        System.out.println("Draws: " + draws + "   Timeouts: " + timeouts);
        System.out.println();
    }

    private static void recordMixed(Map<String, CharacterAgg> stats, BattleResult result) {
        boolean aWon = result.getWinner() == BattleResult.Winner.TEAM_A;
        boolean bWon = result.getWinner() == BattleResult.Winner.TEAM_B;
        for (BattleResult.FighterSnapshot fighter : result.getFighters()) {
            CharacterAgg agg = stats.get(fighter.getName());
            if (agg == null) {
                continue;
            }
            boolean teamWon = "A".equals(fighter.getTeamId()) ? aWon : bWon;
            agg.record(teamWon, !fighter.isFainted(), fighter.hpPercent());
        }
    }

    private static List<CharacterTemplate> randomTeam(List<CharacterTemplate> roster, RandomProvider random) {
        List<CharacterTemplate> team = new ArrayList<>();
        for (int i = 0; i < TEAM_SIZE; i++) {
            team.add(roster.get(random.nextInt(0, roster.size() - 1)));
        }
        return team;
    }

    static List<int[]> combinations(int n, int k) {
        List<int[]> out = new ArrayList<>();
        combinations(n, k, 0, new int[k], 0, out);
        return out;
    }

    private static void combinations(int n, int k, int start, int[] current, int depth, List<int[]> out) {
        if (depth == k) {
            out.add(current.clone());
            return;
        }
        for (int i = start; i <= n - (k - depth); i++) {
            current[depth] = i;
            combinations(n, k, i + 1, current, depth + 1, out);
        }
    }

    private static String joinNames(List<CharacterTemplate> templates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < templates.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(templates.get(i).getName());
        }
        return sb.toString();
    }

    private static String truncate(String name, int max) {
        if (name.length() <= max) {
            return name;
        }
        return name.substring(0, max);
    }

    /**
     * How far each solo / duo / trio climbs {@link com.battlesim.dungeon.Dungeon#standard()}.
     * Fill this in after DungeonRun.run actually fights waves.
     */
    private static void runDungeonProgress(List<CharacterTemplate> roster, RandomProvider random) {
        System.out.println("Dungeon climb (skeleton — DungeonRun is not implemented yet)");
        // TODO: filter out templates with no moves (e.g. Dual Swordsman)
        // TODO: solos — for each template, RUNS times: spawn, DungeonRun.run, record wavesCleared
        // TODO: duos  — combinations(roster.size(), 2)
        // TODO: trios — combinations(roster.size(), 3)
        // TODO: print avg / median wave reached per composition
        // Tune RUNS_PER_COMP: 9+C(9,2)+C(9,3) = 129 comps; 20 runs each is a long sim.
    }

    private static double pct(int count, int total) {
        if (total == 0) {
            return 0;
        }
        return (count * 100.0) / total;
    }

    private static final class RankRow {
        final String name;
        final double pct;
        final int wins;
        final int fights;

        RankRow(String name, double pct, int wins, int fights) {
            this.name = name;
            this.pct = pct;
            this.wins = wins;
            this.fights = fights;
        }
    }

    private static final class CharacterAgg {
        final String name;
        int appearances;
        int teamWins;
        int survived;
        double hpPercentSumWhenTeamWon;
        int teamWinSamples;

        CharacterAgg(String name) {
            this.name = name;
        }

        void record(boolean teamWon, boolean alive, double hpPercent) {
            appearances++;
            if (alive) {
                survived++;
            }
            if (teamWon) {
                teamWins++;
                hpPercentSumWhenTeamWon += hpPercent;
                teamWinSamples++;
            }
        }

        double winRate() {
            return pct(teamWins, appearances);
        }

        double surviveRate() {
            return pct(survived, appearances);
        }

        double avgHpWhenTeamWon() {
            if (teamWinSamples == 0) {
                return 0;
            }
            return hpPercentSumWhenTeamWon / teamWinSamples;
        }
    }
}
