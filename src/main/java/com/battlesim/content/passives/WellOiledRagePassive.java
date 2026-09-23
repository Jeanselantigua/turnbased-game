package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Below half health, attacks deal 35% more damage to every living enemy
 * and also strike living allies. Ally hits are damage only.
 */
public class WellOiledRagePassive implements Passive {

    public static final double RAGE_HP_FRACTION = 0.50;
    public static final double DAMAGE_BONUS = 0.35;

    private final Set<Character> splashAllies = new LinkedHashSet<>();
    private boolean announced;

    public static boolean isRaging(Character self) {
        return self != null
                && !self.isFainted()
                && self.getStats().getCurrentHp() * 2 < self.getStats().getMaxHp();
    }

    @Override
    public List<Character> expandTargets(Character self, Move move, List<Character> chosen,
                                          BattleContext context, List<String> log) {
        splashAllies.clear();
        if (move == null || move.targetsAllies() || context == null || !isRaging(self)) {
            if (!isRaging(self)) {
                announced = false;
            }
            return chosen;
        }
        Set<Character> targets = new LinkedHashSet<>();
        for (Character enemy : context.enemiesOf(self)) {
            if (!enemy.isFainted()) {
                targets.add(enemy);
            }
        }
        List<Character> alliesHit = new ArrayList<>();
        for (Character ally : context.alliesOf(self)) {
            if (ally != self && !ally.isFainted()) {
                targets.add(ally);
                alliesHit.add(ally);
            }
        }
        splashAllies.addAll(alliesHit);
        if (targets.isEmpty()) {
            return chosen;
        }
        if (!announced) {
            announced = true;
            log.add(self.getName() + " enters a blind fury!");
        }
        if (!alliesHit.isEmpty()) {
            log.add("Well Oiled Rage catches allies in the crossfire!");
        }
        return new ArrayList<>(targets);
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (!isRaging(self) || move == null || move.targetsAllies() || splashAllies.contains(target)) {
            return damage;
        }
        return damage * (1.0 + DAMAGE_BONUS);
    }

    @Override
    public boolean suppressesOutgoingStatus(Character self, Character target, Move move) {
        return splashAllies.contains(target);
    }
}
