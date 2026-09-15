package com.battlesim.util;

import java.util.Random;

/**
 * Thin wrapper around randomness so tests can inject deterministic
 * values instead of relying on real random numbers.
 */
public class RandomProvider {

    private final Random random;

    public RandomProvider() {
        this.random = new Random();
    }

    public RandomProvider(long seed) {
        this.random = new Random(seed);
    }

    /** Returns a double in [0.0, 1.0), useful for accuracy/crit checks. */
    public double nextDouble() {
        return random.nextDouble();
    }

    /** Returns an int in [min, max], inclusive — useful for damage variance. */
    public int nextInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
