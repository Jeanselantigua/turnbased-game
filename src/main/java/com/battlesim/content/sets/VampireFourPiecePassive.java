package com.battlesim.content.sets;

import com.battlesim.item.SetBonuses;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

/** Vampire 4-piece: each kill raises max HP by 4.5%. */
public final class VampireFourPiecePassive implements Passive {

    @Override
    public void onKill(Character self, Character victim, Move move, BattleContext context, List<String> log) {
        if (self == null || self.isFainted()) {
            return;
        }
        int gain = SetBonuses.percentOf(self.getStats().getMaxHp(), SetBonuses.VAMPIRE_KILL_MAX_HP);
        if (gain <= 0) {
            gain = 1;
        }
        self.getStats().increaseMaxHp(gain);
        if (log != null) {
            log.add(self.getName() + "'s Vampire set grows (+" + gain + " max HP)!");
        }
    }
}
