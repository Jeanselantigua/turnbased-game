package com.battlesim.content.sets;

import com.battlesim.item.SetBonuses;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Passive;
import java.util.List;

/**
 * Bulwark 4-piece: crossing below 50% HP grants a shield equal to 40% of max HP.
 * Retriggers after healing back above 50%.
 */
public final class BulwarkFourPiecePassive implements Passive {

    private boolean shieldArmed = true;

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        tryGrant(self, log);
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        tryGrant(self, log);
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        tryGrant(self, log);
    }

    @Override
    public void onHealed(Character self, int amount, List<String> log) {
        tryGrant(self, log);
    }

    private void tryGrant(Character self, List<String> log) {
        if (self == null || self.isFainted()) {
            return;
        }
        boolean low = SetBonuses.belowFraction(self, SetBonuses.BULWARK_SHIELD_HP);
        if (!low) {
            shieldArmed = true;
            return;
        }
        if (!shieldArmed) {
            return;
        }
        int amount = SetBonuses.percentOf(self.getStats().getMaxHp(), SetBonuses.BULWARK_SHIELD_PERCENT);
        if (amount <= 0) {
            return;
        }
        self.getStats().grantShield(amount);
        shieldArmed = false;
        if (log != null) {
            log.add(self.getName() + "'s Bulwark set raises a " + amount + " HP shield!");
        }
    }
}
