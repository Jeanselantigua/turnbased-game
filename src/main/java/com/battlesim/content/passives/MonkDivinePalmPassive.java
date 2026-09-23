package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Formation;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

/**
 * Divine Palm also hits fighters standing next to the target for 40% of
 * the damage the original palm dealt.
 */
public class MonkDivinePalmPassive implements Passive {

    public static final double SPLASH_PERCENT = 0.40;

    private boolean splashing;

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log,
                             BattleContext context) {
        if (splashing || context == null || damageDealt <= 0 || move == null) {
            return;
        }
        if (!MonkPerfectEnlightenmentPassive.DIVINE_PALM_NAME.equals(move.getName())) {
            return;
        }
        int splash = (int) Math.round(damageDealt * SPLASH_PERCENT);
        if (splash <= 0) {
            return;
        }
        splashing = true;
        try {
            for (Character neighbor : Formation.adjacentLiving(target, context.enemiesOf(self))) {
                int absorbed = neighbor.getStats().absorbWithShield(splash);
                int leftover = splash - absorbed;
                int actual = leftover > 0 ? neighbor.getStats().applyDamage(leftover) : 0;
                int dealt = absorbed + actual;
                if (dealt > 0) {
                    log.add(neighbor.getName() + " is clipped by Divine Palm for " + dealt + " damage!");
                }
                if (neighbor.isFainted()) {
                    log.add(neighbor.getName() + " has fainted!");
                    context.notifyFaint(neighbor, self, move, log);
                }
            }
        } finally {
            splashing = false;
        }
    }
}
