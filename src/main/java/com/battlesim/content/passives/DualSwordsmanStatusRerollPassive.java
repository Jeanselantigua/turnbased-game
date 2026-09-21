package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import java.util.List;
import java.util.Random;

public class DualSwordsmanStatusRerollPassive implements Passive {

    static final double REROLL_CHANCE = 0.30;
    private final Random random;

    public DualSwordsmanStatusRerollPassive() {
        this(new Random());
    }

    DualSwordsmanStatusRerollPassive(Random random) {
        this.random = random;
    }

    @Override
    public boolean shouldRerollFailedStatus(Character self, Character target, Move move,
                                             List<String> log) {
        if (move.getInflictedStatus() == Status.NONE || random.nextDouble() >= REROLL_CHANCE) {
            return false;
        }
        log.add(self.getName() + " presses the status from " + move.getName() + "!");
        return true;
    }

    @Override
    public int modifyOutgoingStatusMagnitude(Character self, Character target, Status status,
                                              int magnitude, List<String> log) {
        if (status == Status.BLEED) {
            log.add(self.getName() + " makes bleed twice as deep!");
            return magnitude * 2;
        }
        if (status == Status.PARALYSIS) {
            int skipTurns = magnitude <= 0 ? 1 : magnitude;
            log.add(self.getName() + " makes paralysis seize for 2 turns!");
            return skipTurns * 2;
        }
        return magnitude;
    }
}
