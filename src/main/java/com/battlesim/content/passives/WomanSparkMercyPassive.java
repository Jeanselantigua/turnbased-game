package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Type;
import java.util.List;

/** Damaging Holy attacks heal the lowest-HP ally for a portion of the damage. */
public class WomanSparkMercyPassive implements Passive {

    private static final double HEAL_PERCENT_OF_DAMAGE = 0.30;

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log,
                             BattleContext context) {
        if (context == null || damageDealt <= 0 || move.getType() != Type.HOLY || move.targetsAllies()) {
            return;
        }

        Character lowest = lowestHp(context.alliesOf(self));
        if (lowest == null || lowest.isFainted()) {
            return;
        }

        int amount = (int) Math.round(damageDealt * HEAL_PERCENT_OF_DAMAGE);
        int missing = Math.max(0, lowest.getStats().getMaxHp() - lowest.getStats().getCurrentHp());
        int healed = lowest.getStats().heal(Math.min(amount, missing));
        if (healed > 0) {
            log.add(self.getName() + "'s holy spark restores " + healed + " HP to "
                    + lowest.getName() + "!");
            for (Passive passive : lowest.getPassives()) {
                passive.onHealed(lowest, healed, log);
            }
        }
    }

    private static Character lowestHp(List<Character> allies) {
        Character lowest = null;
        for (Character ally : allies) {
            if (ally.isFainted() || ally.isSummon()) {
                continue;
            }
            if (lowest == null || ally.getStats().getCurrentHp() < lowest.getStats().getCurrentHp()) {
                lowest = ally;
            }
        }
        return lowest;
    }
}
