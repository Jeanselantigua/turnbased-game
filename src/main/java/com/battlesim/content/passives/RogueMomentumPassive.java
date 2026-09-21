package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

/**
 * Stacks attack and speed on each kill. After three of the owner's turns
 * with no kill, every stack falls off at once.
 */
public class RogueMomentumPassive implements Passive {

    public static final int SPEED_PER_STACK = 5;
    public static final int ATTACK_PER_STACK = 3;
    public static final int TURNS_WITHOUT_KILL_TO_EXPIRE = 3;

    private int stacks = 0;
    private int turnsWithoutKill = 0;

    @Override
    public void onKill(Character self, Character victim, Move move, BattleContext context, List<String> log) {
        if (self == null || self.isFainted() || victim == null || victim == self) {
            return;
        }
        stacks++;
        turnsWithoutKill = 0;
        self.getStats().increaseSpeed(SPEED_PER_STACK);
        self.getStats().increaseAttack(ATTACK_PER_STACK);
        log.add(self.getName() + " gains Momentum! (" + stacks + " stack"
                + (stacks == 1 ? "" : "s") + ": +"
                + (stacks * SPEED_PER_STACK) + " speed, +"
                + (stacks * ATTACK_PER_STACK) + " attack)");
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        if (stacks <= 0 || self == null || self.isFainted()) {
            return;
        }
        turnsWithoutKill++;
        if (turnsWithoutKill >= TURNS_WITHOUT_KILL_TO_EXPIRE) {
            int lostSpeed = stacks * SPEED_PER_STACK;
            int lostAttack = stacks * ATTACK_PER_STACK;
            self.getStats().increaseSpeed(-lostSpeed);
            self.getStats().increaseAttack(-lostAttack);
            stacks = 0;
            turnsWithoutKill = 0;
            log.add(self.getName() + "'s Momentum fades.");
        }
    }
}
