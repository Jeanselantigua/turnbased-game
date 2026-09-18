package com.battlesim.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Outcome of a finished (or timed-out) battle. */
public class BattleResult {

    public enum Winner {
        TEAM_A,
        TEAM_B,
        DRAW,
        TIMEOUT
    }

    public static class FighterSnapshot {
        private final String teamId;
        private final String name;
        private final int currentHp;
        private final int maxHp;

        public FighterSnapshot(String teamId, String name, int currentHp, int maxHp) {
            this.teamId = teamId;
            this.name = name;
            this.currentHp = currentHp;
            this.maxHp = maxHp;
        }

        public String getTeamId() { return teamId; }
        public String getName() { return name; }
        public int getCurrentHp() { return currentHp; }
        public int getMaxHp() { return maxHp; }
        public boolean isFainted() { return currentHp <= 0; }

        public double hpPercent() {
            if (maxHp <= 0) {
                return 0;
            }
            return (currentHp * 100.0) / maxHp;
        }
    }

    private final Winner winner;
    private final int actionCount;
    private final List<FighterSnapshot> fighters;
    private final List<String> log;

    public BattleResult(Winner winner, int actionCount,
                        List<FighterSnapshot> fighters, List<String> log) {
        this.winner = winner;
        this.actionCount = actionCount;
        this.fighters = Collections.unmodifiableList(new ArrayList<>(fighters));
        this.log = Collections.unmodifiableList(new ArrayList<>(log));
    }

    public Winner getWinner() { return winner; }
    public int getActionCount() { return actionCount; }
    public List<FighterSnapshot> getFighters() { return fighters; }
    public List<String> getLog() { return log; }
}
