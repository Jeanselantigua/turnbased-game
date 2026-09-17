package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;
import java.util.Random;

public class RogueCritStealthPassive implements Passive {

    private static final double CRIT_CHANCE = 0.30;
    private final Random random = new Random();
    private boolean stealthed = false;

    @Override
    public boolean isUntargetable(Character self) {
        return stealthed;
    }

    @Override
    public boolean rollBonusCrit(Character self, Move move) {
        return random.nextDouble() < CRIT_CHANCE;
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        double multiplier = 1.0;
        if (isCrit) {
            log.add(self.getName() + " lands a critical hit!");
            multiplier *= 2.0;
        }
        if (stealthed) {
            log.add(self.getName() + " strikes from the shadows for double damage!");
            multiplier *= 2.0;
        }
        return damage * multiplier;
    }

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log) {
        if (stealthed && damageDealt > 0) {
            stealthed = false;
        }
        if (isCrit) {
            stealthed = true;
            log.add(self.getName() + " vanishes into the shadows!");
        }
    }
}