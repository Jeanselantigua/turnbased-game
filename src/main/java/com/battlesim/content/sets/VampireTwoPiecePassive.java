package com.battlesim.content.sets;

import com.battlesim.item.SetBonuses;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

/** Vampire 2-piece: physical hits heal 25% of damage dealt. */
public final class VampireTwoPiecePassive implements Passive {

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                            int damageDealt, boolean isCrit, List<String> log,
                            BattleContext context) {
        if (self == null || self.isFainted() || move == null || move.isMagic() || damageDealt <= 0) {
            return;
        }
        int heal = SetBonuses.percentOf(damageDealt, SetBonuses.VAMPIRE_LIFESTEAL);
        if (heal <= 0) {
            return;
        }
        int gained = self.getStats().heal(heal);
        if (gained > 0 && log != null) {
            log.add(self.getName() + "'s Vampire set siphons " + gained + " HP!");
        }
    }
}
