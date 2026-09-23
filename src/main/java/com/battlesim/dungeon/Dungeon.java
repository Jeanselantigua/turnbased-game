package com.battlesim.dungeon;

import com.battlesim.content.Enemies;
import com.battlesim.model.EnemyTemplate;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * A sequence of waves. Enemy stats start at {@link #STARTING_SCALE}, stay
 * flat for {@link #BOSS_EVERY} floors, then jump by {@link #SCALE_PER_BLOCK}.
 * Those same floors are boss fights. Solo and duo parties also get a lower
 * {@link #partySizeScale(int)}.
 */
public class Dungeon {

    public static final int BOSS_EVERY = 15;
    public static final int WAYPOINT_EVERY = 10;
    public static final int DEFAULT_FLOORS = 100;
    /** Opening block is weaker so early floors are not the spike. */
    public static final double STARTING_SCALE = 0.85;
    public static final double SCALE_PER_BLOCK = 0.20;
    /** Enemy stat multiplier vs a trio. Solo and duo fights are scaled down. */
    public static final double SOLO_SCALE = 0.75;
    public static final double DUO_SCALE = 0.90;
    public static final double TRIO_SCALE = 1.00;

    private final List<Wave> waves;
    private final double scalePerBlock;
    private final int blockSize;
    private final double startingScale;

    public Dungeon(List<Wave> waves, double scalePerBlock) {
        this(waves, scalePerBlock, BOSS_EVERY);
    }

    public Dungeon(List<Wave> waves, double scalePerBlock, int blockSize) {
        this(waves, scalePerBlock, blockSize, 1.0);
    }

    public Dungeon(List<Wave> waves, double scalePerBlock, int blockSize, double startingScale) {
        if (waves == null || waves.isEmpty()) {
            throw new IllegalArgumentException("A dungeon needs at least one wave");
        }
        if (blockSize < 1) {
            throw new IllegalArgumentException("blockSize must be at least 1");
        }
        this.waves = List.copyOf(waves);
        this.scalePerBlock = scalePerBlock;
        this.blockSize = blockSize;
        this.startingScale = startingScale;
    }

    public static Dungeon standard() {
        return standard(new RandomProvider(1L));
    }

    public static Dungeon standard(RandomProvider random) {
        return generate(DEFAULT_FLOORS, random);
    }

    public static Dungeon generate(int floors, RandomProvider random) {
        if (floors < 1) {
            throw new IllegalArgumentException("A dungeon needs at least one wave");
        }
        List<EnemyTemplate> normals = Enemies.normals();
        List<EnemyTemplate> elites = Enemies.elites();
        List<EnemyTemplate> bosses = Enemies.bosses();
        List<Wave> waves = new ArrayList<>();
        for (int waveNumber = 1; waveNumber <= floors; waveNumber++) {
            waves.add(buildWave(waveNumber, normals, elites, bosses, random));
        }
        return new Dungeon(waves, SCALE_PER_BLOCK, BOSS_EVERY, STARTING_SCALE);
    }

    public static boolean isBossWave(int waveNumber) {
        return waveNumber > 0 && waveNumber % BOSS_EVERY == 0;
    }

    /** Rest-or-chest stop after a cleared floor, if the climb continues. */
    public static boolean isWaypointWave(int waveNumber) {
        return waveNumber > 0 && waveNumber % WAYPOINT_EVERY == 0;
    }

    private static Wave buildWave(int waveNumber,
                                  List<EnemyTemplate> normals,
                                  List<EnemyTemplate> elites,
                                  List<EnemyTemplate> bosses,
                                  RandomProvider random) {
        if (isBossWave(waveNumber)) {
            return new Wave(List.of(pick(bosses, random)));
        }
        int slotInBlock = ((waveNumber - 1) % BOSS_EVERY) + 1;
        if (slotInBlock >= 11) {
            return new Wave(List.of(pick(normals, random), pick(elites, random)));
        }
        return new Wave(List.of(pick(normals, random), pick(normals, random)));
    }

    private static EnemyTemplate pick(List<EnemyTemplate> pool, RandomProvider random) {
        return pool.get(random.nextInt(0, pool.size() - 1));
    }

    public List<Wave> getWaves() {
        return waves;
    }

    /** Waves 1–15 = {@link #STARTING_SCALE}, then +{@link #SCALE_PER_BLOCK} each block. */
    public double scaleForWave(int waveNumber) {
        int block = Math.max(0, waveNumber - 1) / blockSize;
        return startingScale + block * scalePerBlock;
    }

    /** Floor scale with a solo / duo / trio handicap. Trio is the baseline (1.0). */
    public double scaleForWave(int waveNumber, int partySize) {
        return scaleForWave(waveNumber) * partySizeScale(partySize);
    }

    public static double partySizeScale(int partySize) {
        if (partySize <= 1) {
            return SOLO_SCALE;
        }
        if (partySize == 2) {
            return DUO_SCALE;
        }
        return TRIO_SCALE;
    }
}
