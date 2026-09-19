package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.util.RandomProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Honkai Star Rail-style Action Value turn order: every Character has a
 * running "next action time" that advances by (BASE_TICKS / speed) each
 * time they act. Faster characters naturally get more turns over time —
 * the frequency falls out of the math rather than a hardcoded threshold.
 */
public class TurnOrderScheduler {

    private static final double BASE_TICKS = 10000.0;

    private final RandomProvider randomProvider;
    private final Map<Character, Double> nextActionTime = new LinkedHashMap<>();

    public TurnOrderScheduler(List<Character> allCombatants, RandomProvider randomProvider) {
        this.randomProvider = randomProvider;
        for (Character character : allCombatants) {
            nextActionTime.put(character, delayFor(character));
        }
    }

    private double delayFor(Character character) {
        return BASE_TICKS / character.getEffectiveSpeed();
    }

    /** Picks the next Character to act (lowest action time; ties broken by coin flip). */
    public Character getNextActor() {
        Character next = null;
        double lowest = Double.MAX_VALUE;

        for (Map.Entry<Character, Double> entry : nextActionTime.entrySet()) {
            Character character = entry.getKey();
            if (character.isFainted()) {
                continue;
            }
            double time = entry.getValue();
            if (time < lowest) {
                lowest = time;
                next = character;
            } else if (time == lowest && randomProvider.nextDouble() < 0.5) {
                next = character; // tie: coin flip decides which one wins
            }
        }
        return next;
    }

    /** Insert a mid-battle summon so they wait one speed delay from the current front of the queue. */
    public void addCombatant(Character character) {
        double soonest = Double.MAX_VALUE;
        for (Double time : nextActionTime.values()) {
            if (time < soonest) {
                soonest = time;
            }
        }
        if (soonest == Double.MAX_VALUE) {
            soonest = 0;
        }
        nextActionTime.put(character, soonest + delayFor(character));
    }

    /** Call after a Character acts, to push their next turn out based on speed. */
    public void advanceActor(Character character) {
        nextActionTime.merge(character, delayFor(character), Double::sum);
    }
}
