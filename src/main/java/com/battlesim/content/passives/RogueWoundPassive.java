package com.battlesim.content.passives;

import com.battlesim.engine.StatusEffectResolver;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

/**
 * Maim applies {@link Status#WOUNDED} stacks (cap {@link Character#WOUND_STACK_CAP}).
 * Assassinate detonates them for {@code stacks × (15 + 4% of the target's max HP)}.
 */
public class RogueWoundPassive implements Passive {

    public static final String MAIM_NAME = "Maim";
    public static final String ASSASSINATE_NAME = "Assassinate";
    public static final int MAIM_MIN_HITS = 1;
    public static final int MAIM_MAX_HITS = 6;
    public static final int MAIM_POWER = 5;
    public static final int WOUND_CHANCE = 15;
    public static final int WOUND_FLAT = 15;
    public static final double WOUND_MAX_HP_PERCENT = 0.025;
    public static final int ASSASSINATE_COOLDOWN = 2;

    public static Move createMaim() {
        return new Move(MAIM_NAME, Type.ARCANE, MAIM_POWER, 90, 0, false,
                Status.WOUNDED, WOUND_CHANCE, 0, MAIM_MIN_HITS, MAIM_MAX_HITS);
    }

    public static Move createAssassinate() {
        return new Move(ASSASSINATE_NAME, Type.ARCANE, 65, 90, 1, false,
                Status.NONE, 0, ASSASSINATE_COOLDOWN);
    }

    public static int detonationDamage(int stacks, int targetMaxHp) {
        if (stacks <= 0) {
            return 0;
        }
        int perStack = WOUND_FLAT + (int) Math.round(targetMaxHp * WOUND_MAX_HP_PERCENT);
        return stacks * perStack;
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        if (move == null || !ASSASSINATE_NAME.equals(move.getName()) || target == null) {
            return;
        }
        if (!target.isWounded()) {
            return;
        }
        int stacks = target.consumeWoundStacks();
        int damage = detonationDamage(stacks, target.getStats().getMaxHp());
        if (damage <= 0) {
            return;
        }
        StatusEffectResolver resolver = new StatusEffectResolver();
        int actual = resolver.applyDamageThroughShield(target, self, damage, context, log);
        log.add(self.getName() + " triggers " + stacks + " wound stack"
                + (stacks == 1 ? "" : "s") + " for " + actual + " damage!");
        if (target.isFainted()) {
            log.add(target.getName() + " has fainted!");
            resolver.notifyFaint(target, self, move, context, log);
        }
    }
}
