package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * When Sion's shield breaks, it explodes and damages every living enemy.
 * The shield itself comes from a {@link Status#SELF_SHIELD} move.
 */
public class SionExplodingShieldPassive implements Passive {

    public static final String MOVE_NAME = "Roar of the slayer";
    public static final int SHIELD_HP = 100;
    public static final int EXPLOSION_DAMAGE = 100;

    private boolean explodePending;

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.UNDEAD, SHIELD_HP, 100, 0, true, Status.SELF_SHIELD, 100);
    }

    @Override
    public void onShieldBroken(Character self, Character attacker, BattleContext context, List<String> log) {
        explodePending = true;
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        if (!explodePending || context == null) {
            return;
        }
        explodePending = false;
        log.add(self.getName() + "'s shattered shield explodes!");
        Move explosion = new Move(MOVE_NAME, Type.UNDEAD, 0, 100, 0, true, Status.NONE, 0);
        for (Character enemy : new ArrayList<>(context.enemiesOf(self))) {
            if (enemy.isFainted()) {
                continue;
            }
            int actual = enemy.getStats().applyDamage(EXPLOSION_DAMAGE);
            log.add(enemy.getName() + " took " + actual + " damage from the explosion!");
            if (enemy.isFainted()) {
                log.add(enemy.getName() + " has fainted!");
                context.notifyFaint(enemy, self, explosion, log);
            }
        }
    }
}
