package com.battlesim.dungeon;

import java.util.List;

/** How far a party got in one dungeon climb. */
public class DungeonResult {

    private final int wavesCleared;
    private final boolean clearedAll;
    private final List<String> log;

    public DungeonResult(int wavesCleared, boolean clearedAll, List<String> log) {
        this.wavesCleared = wavesCleared;
        this.clearedAll = clearedAll;
        this.log = List.copyOf(log);
    }

    public int getWavesCleared() {
        return wavesCleared;
    }

    public boolean clearedAll() {
        return clearedAll;
    }

    public List<String> getLog() {
        return log;
    }
}
