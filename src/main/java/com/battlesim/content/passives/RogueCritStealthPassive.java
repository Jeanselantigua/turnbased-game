package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;
import java.util.Random;

public class RogueCritStealthPassive implements Passive {

    private static final double CRIT_CHANCE = 0.15;
    private static final double CRIT_MULTIPLIER = 2.0;
    private static final double STEALTH_MULTIPLIER = 1.5;
    private static final int STEALTH_EVASION = 35;
    private final Random random = new Random();
    private boolean stealthed = false;

    @Override
    public boolean isStealthed(Character self) {
        return stealthed;
    }

    @Override
    public boolean rollBonusCrit(Character self, Move move) {
        return random.nextDouble() < CRIT_CHANCE;
    }

    @Override
    public int modifyIncomingAccuracy(Character self, Character attacker, Move move, int accuracy) {
        if (!stealthed) {
            return accuracy;
        }
        return accuracy - STEALTH_EVASION;
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        double multiplier = 1.0;
        if (isCrit) {
            log.add(self.getName() + " lands a critical hit!");
            multiplier *= CRIT_MULTIPLIER;
        }
        if (stealthed) {
            log.add(self.getName() + " strikes from the shadows!");
            multiplier *= STEALTH_MULTIPLIER;
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

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        if (stealthed && damageTaken > 0) {
            stealthed = false;
            log.add(self.getName() + " is forced out of the shadows!");
        }
    }
}
