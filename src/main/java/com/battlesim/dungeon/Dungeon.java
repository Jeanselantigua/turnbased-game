package com.battlesim.dungeon;

import com.battlesim.content.Enemies;
import java.util.ArrayList;
import java.util.List;

/**
 * A sequence of waves. Later floors should scale via {@link #scaleForWave(int)}.
 * Replace {@link #standard()} with a real mix once Enemies has real content.
 */
public class Dungeon {

    private final List<Wave> waves;
    private final double scalePerWave;

    public Dungeon(List<Wave> waves, double scalePerWave) {
        if (waves == null || waves.isEmpty()) {
            throw new IllegalArgumentException("A dungeon needs at least one wave");
        }
        this.waves = List.copyOf(waves);
        this.scalePerWave = scalePerWave;
    }

    public static Dungeon standard() {
        List<Wave> waves = new ArrayList<>();
        waves.add(new Wave(List.of(Enemies.goblin(), Enemies.goblin())));
        waves.add(new Wave(List.of(Enemies.goblin(), Enemies.eliteDummy())));
        waves.add(new Wave(List.of(Enemies.bossDummy())));
        // TODO: longer climb, random picks from Enemies.normals/elites/bosses, named floors
        return new Dungeon(waves, 0.15);
    }

    public List<Wave> getWaves() {
        return waves;
    }

    /** Wave 1 = 1.0, wave 2 = 1.0 + scalePerWave, etc. */
    public double scaleForWave(int waveNumber) {
        return 1.0 + Math.max(0, waveNumber - 1) * scalePerWave;
    }
}
