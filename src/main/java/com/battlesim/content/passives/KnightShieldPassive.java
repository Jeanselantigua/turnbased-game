package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;
import java.util.Random;

public class KnightShieldPassive implements Passive {

    private static final double BLOCK_CHANCE = 0.1;
    private final Random random = new Random();

    @Override
    public double modifyIncomingDamage(Character self, Character attacker, Move move,
                                        double damage, List<String> log) {
        if (random.nextDouble() < BLOCK_CHANCE) {
            log.add(self.getName() + " blocks the attack completely!");
            return 0;
        }
        return damage;
    }
}