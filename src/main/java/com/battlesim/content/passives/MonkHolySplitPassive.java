package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

public class MonkHolySplitPassive implements Passive {

    private static final double BONUS_PERCENT_MAGIC_ATTACK = 0.60;

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (move.getPower() == 0) {
            return damage;
        }
        double bonus = self.getStats().getMagicAttack() * BONUS_PERCENT_MAGIC_ATTACK;
        log.add(self.getName() + "'s holy power adds " + Math.round(bonus) + " bonus damage!");
        return damage + bonus;
    }
}