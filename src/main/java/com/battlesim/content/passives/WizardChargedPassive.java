package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;
import java.util.List;

public class WizardChargedPassive implements Passive {

    private static final int SPEED_BONUS = 10;
    private static final int MAGIC_BONUS = 10;
    private boolean charged = false;

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        refresh(self, log);
    }

    @Override
    public void onHealed(Character self, int amount, List<String> log) {
        refresh(self, log);
    }

    private void refresh(Character self, List<String> log) {
        boolean shouldCharge = !self.isFainted()
                && self.getStats().getCurrentHp() * 2 < self.getStats().getMaxHp();
        if (shouldCharge && !charged) {
            charged = true;
            self.getStats().increaseSpeed(SPEED_BONUS);
            self.getStats().increaseMagicAttack(MAGIC_BONUS);
            log.add(self.getName() + " becomes charged! (+" + SPEED_BONUS
                    + " speed, +" + MAGIC_BONUS + " magic attack)");
        } else if (!shouldCharge && charged) {
            charged = false;
            self.getStats().increaseSpeed(-SPEED_BONUS);
            self.getStats().increaseMagicAttack(-MAGIC_BONUS);
            log.add(self.getName() + " is no longer charged.");
        }
    }
}
