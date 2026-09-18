package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;

/**
 * Offsets stealth evasion so Volt can still tag a hidden Rogue.
 */
public class WizardHuntPassive implements Passive {

    private static final int STEALTH_ACCURACY_BONUS = 35;

    @Override
    public int modifyAccuracy(Character self, Character target, Move move, int baseAccuracy) {
        for (Passive passive : target.getPassives()) {
            if (passive.isStealthed(target)) {
                return baseAccuracy + STEALTH_ACCURACY_BONUS;
            }
        }
        return baseAccuracy;
    }
}
