package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

/**
 * Prideful Taunt pulls single-target attacks onto the champion for two of
 * their turns, raises defense by 20%, and cuts a killing blow's damage by 60%.
 */
public class PridefulTauntPassive implements Passive {

    public static final String MOVE_NAME = "Prideful Taunt";
    public static final int TURNS = 2;
    public static final double DEFENSE_BONUS = 0.20;
    public static final double LETHAL_REMAINING = 0.40;

    private int turnsRemaining;

    public boolean isTaunting() {
        return turnsRemaining > 0;
    }

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.PHYSICAL, 0, 100, 0, false, Status.SELF_SHIELD, 0);
    }

    @Override
    public void onActionResolved(Character self, Move move, List<Character> targets,
                                  BattleContext context, List<String> log) {
        if (move == null || !MOVE_NAME.equals(move.getName())) {
            return;
        }
        turnsRemaining = TURNS;
        log.add(self.getName() + " boasts without fear. All eyes turn to them!");
    }

    @Override
    public boolean forcesAggro(Character self) {
        return turnsRemaining > 0 && self != null && !self.isFainted();
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        if (turnsRemaining <= 0) {
            return;
        }
        turnsRemaining--;
        if (turnsRemaining == 0) {
            log.add(self.getName() + "'s taunt fades.");
        }
    }

    @Override
    public double modifyIncomingDamage(Character self, Character attacker, Move move,
                                        double damage, List<String> log) {
        if (turnsRemaining <= 0 || damage <= 0) {
            return damage;
        }
        double reduced = damage / (1.0 + DEFENSE_BONUS);
        int pool = self.getStats().getCurrentHp() + self.getStats().getShieldHp();
        if (pool > 0 && reduced >= pool) {
            log.add(self.getName() + "'s pride turns the killing blow aside!");
            reduced *= LETHAL_REMAINING;
        }
        return reduced;
    }
}
