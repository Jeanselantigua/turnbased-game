package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import java.util.List;

/** Extra damage against siphoned or otherwise afflicted targets. */
public class ChefSlowCookPassive implements Passive {

    private static final double BONUS = 0.20;

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (!target.isSiphoned() && target.getStatus() == Status.NONE) {
            return damage;
        }
        log.add(self.getName() + "'s slow cook extra-tenderizes " + target.getName() + "!");
        return damage * (1.0 + BONUS);
    }
}
