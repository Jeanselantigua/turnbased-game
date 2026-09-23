package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

/**
 * Decamating Slam winds up for one of Sion's turns, then slams the stored target.
 */
public class SionSlamPassive implements Passive {

    public static final String MOVE_NAME = "Decamating Slam";

    private boolean windingUp;
    private boolean releasing;
    private Character storedTarget;
    private Move slam;

    public static boolean isSlam(Move move) {
        return move != null && MOVE_NAME.equals(move.getName())
                && move.getInflictedStatus() != Status.UTILITY;
    }

    @Override
    public boolean skipsOwnAction(Character self) {
        return windingUp;
    }

    @Override
    public Move rewriteMove(Character self, Move move, List<Character> targets,
                             BattleContext context, List<String> log) {
        if (releasing) {
            return move;
        }
        if (!isSlam(move)) {
            return move;
        }
        slam = move;
        storedTarget = first(targets);
        windingUp = true;
        log.add(self.getName() + " begins charging " + MOVE_NAME + "!");
        return new Move(MOVE_NAME, Type.PHYSICAL, 0, 100, 0, false, Status.UTILITY, 100);
    }

    @Override
    public void onActionSkipped(Character self, BattleContext context, List<String> log) {
        if (!windingUp) {
            return;
        }
        windingUp = false;
        if (self.isFainted() || self.hasStatus(Status.STUN)) {
            releasing = false;
            log.add(self.getName() + "'s " + MOVE_NAME + " is interrupted!");
            return;
        }
        Character target = livingTarget(context, self);
        if (target == null || slam == null) {
            log.add(self.getName() + "'s " + MOVE_NAME + " has no target!");
            return;
        }
        releasing = true;
        log.add(self.getName() + " unleashes " + MOVE_NAME + "!");
        self.queueAction(slam, List.of(target));
    }

    @Override
    public void onActionResolved(Character self, Move move, List<Character> targets,
                                  BattleContext context, List<String> log) {
        releasing = false;
    }

    private Character livingTarget(BattleContext context, Character self) {
        if (storedTarget != null && !storedTarget.isFainted()) {
            return storedTarget;
        }
        if (context == null) {
            return null;
        }
        for (Character enemy : context.enemiesOf(self)) {
            if (!enemy.isFainted()) {
                return enemy;
            }
        }
        return null;
    }

    private static Character first(List<Character> targets) {
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        return targets.get(0);
    }
}
