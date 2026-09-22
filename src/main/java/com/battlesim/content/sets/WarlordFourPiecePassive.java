package com.battlesim.content.sets;

import com.battlesim.item.SetBonusPassive;
import com.battlesim.item.SetBonuses;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.StatKind;
import java.util.List;

/**
 * Warlord 4-piece: +20% / +40% physical attack while 1 / 2+ non-summon allies
 * are fainted. Percent is of attack excluding Warlord set bonuses.
 */
public final class WarlordFourPiecePassive implements SetBonusPassive {

    private int appliedAttack;

    public int getAppliedAttack() {
        return appliedAttack;
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        refresh(self, context, log);
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        refresh(self, context, log);
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken,
                              List<String> log, BattleContext context) {
        refresh(self, context, log);
    }

    @Override
    public void clearBonus(Character wearer) {
        applyAttack(wearer, 0, null);
    }

    private void refresh(Character self, BattleContext context, List<String> log) {
        if (self == null || self.isFainted()) {
            applyAttack(self, 0, log);
            return;
        }
        int dead = SetBonuses.faintedAllies(self, context);
        double percent = 0;
        if (dead >= 2) {
            percent = SetBonuses.WARLORD_TWO_DEAD_ATTACK;
        } else if (dead >= 1) {
            percent = SetBonuses.WARLORD_ONE_DEAD_ATTACK;
        }
        int base = SetBonuses.attackWithoutWarlordSet(self, this);
        applyAttack(self, SetBonuses.percentOf(base, percent), log);
    }

    private void applyAttack(Character wearer, int wanted, List<String> log) {
        if (wearer == null || wanted == appliedAttack) {
            return;
        }
        wearer.getStats().add(StatKind.ATTACK, wanted - appliedAttack);
        appliedAttack = wanted;
        if (log != null && wanted > 0) {
            log.add(wearer.getName() + "'s Warlord set surges (+" + wanted + " Attack)!");
        }
    }
}
