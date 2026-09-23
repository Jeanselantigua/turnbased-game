package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
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
        return pickNext(nextActionTime, true);
    }

    /**
     * Upcoming living actors without changing the real queue. AV is absolute
     * next-action time; the UI subtracts the front of the list so "now" is 0.
     * Ties keep insertion order so the sidebar does not flicker.
     */
    public List<UpcomingTurn> preview(int count) {
        List<UpcomingTurn> upcoming = new ArrayList<>();
        if (count <= 0) {
            return upcoming;
        }
        Map<Character, Double> simulated = new LinkedHashMap<>(nextActionTime);
        for (int i = 0; i < count; i++) {
            Character next = pickNext(simulated, false);
            if (next == null) {
                break;
            }
            double time = simulated.get(next);
            upcoming.add(new UpcomingTurn(next, time));
            simulated.merge(next, delayFor(next), Double::sum);
        }
        return upcoming;
    }

    private Character pickNext(Map<Character, Double> times, boolean randomTies) {
        Character next = null;
        double lowest = Double.MAX_VALUE;

        for (Map.Entry<Character, Double> entry : times.entrySet()) {
            Character character = entry.getKey();
            if (character.isFainted()) {
                continue;
            }
            double time = entry.getValue();
            if (time < lowest) {
                lowest = time;
                next = character;
            } else if (time == lowest && randomTies && randomProvider.nextDouble() < 0.5) {
                next = character;
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

    /** One predicted turn in {@link #preview(int)}. */
    public static final class UpcomingTurn {
        private final Character character;
        private final double actionTime;

        UpcomingTurn(Character character, double actionTime) {
            this.character = character;
            this.actionTime = actionTime;
        }

        public Character getCharacter() {
            return character;
        }

        public double getActionTime() {
            return actionTime;
        }

        /** Rounded AV from the current front of the queue (0 = acting now). */
        public int delayFrom(double now) {
            return (int) Math.round(actionTime - now);
        }
    }
}
