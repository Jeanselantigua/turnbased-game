package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;
import java.util.Random;

public class KnightBleedPassive implements Passive {

    private static final double PROC_CHANCE = 0.20;
    private final Random random = new Random();

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log) {
        if (move.getType() != Type.PHYSICAL) {
            return;
        }
        if (target.getStatus() != Status.NONE) {
            return;
        }
        if (random.nextDouble() < PROC_CHANCE) {
            target.setStatus(Status.BLEED);
            log.add(target.getName() + " is bleeding from the Knight's strike!");
        }
    }
}