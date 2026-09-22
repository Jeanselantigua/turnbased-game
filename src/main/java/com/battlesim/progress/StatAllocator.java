package com.battlesim.progress;

import com.battlesim.model.Character;

/** Spends unspent stat points after a level-up (console, AI, tests). */
public interface StatAllocator {

    void allocate(Character character);
}
