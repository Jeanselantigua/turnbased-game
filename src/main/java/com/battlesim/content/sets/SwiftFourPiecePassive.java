package com.battlesim.content.sets;

import com.battlesim.item.SetBonuses;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;

/**
 * Swift 4-piece: +20% crit damage above 60 Speed, +40% above 80.
 */
public final class SwiftFourPiecePassive implements Passive {

    @Override
    public int modifyCritDamage(Character self, Move move, int damagePercent) {
        if (self == null) {
            return damagePercent;
        }
        int bonus = (int) Math.round(SetBonuses.swiftCritDamageBonus(self.getStats().getSpeed()) * 100);
        return damagePercent + bonus;
    }
}
