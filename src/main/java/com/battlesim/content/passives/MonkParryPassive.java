package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Type;
import java.util.List;
import java.util.Random;

public class MonkParryPassive implements Passive {

    private static final double PARRY_CHANCE = 0.20;
    private final Random random = new Random();

    @Override
    public double modifyIncomingDamage(Character self, Character attacker, Move move,
                                        double damage, List<String> log) {
        if (move.getType() != Type.PHYSICAL) {
            return damage;
        }
        if (random.nextDouble() < PARRY_CHANCE) {
            int reflected = (int) Math.round(damage * 0.5);
            int actualReflected = attacker.getStats().applyDamage(reflected);
            log.add(self.getName() + " parries and reflects " + actualReflected
                    + " damage back at " + attacker.getName() + "!");
            return 0;
        }
        return damage;
    }
}