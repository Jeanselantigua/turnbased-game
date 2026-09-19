package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

/** Permanently gains max HP on each kill, keeping current HP at the same percent. */
public class SionKillMaxHpPassive implements Passive {

    public static final int HP_PER_KILL = 40;

    @Override
    public void onKill(Character self, Character victim, Move move, BattleContext context, List<String> log) {
        if (self == null || self.isFainted() || victim == null || victim == self) {
            return;
        }
        self.getStats().increaseMaxHpPreservingPercent(HP_PER_KILL);
        log.add(self.getName() + " grows stronger, max HP is now "
                + self.getStats().getMaxHp() + "!");
    }
}
