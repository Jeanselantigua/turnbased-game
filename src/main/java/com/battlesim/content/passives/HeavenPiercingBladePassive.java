package com.battlesim.content.passives;

import com.battlesim.engine.StatusEffectResolver;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import java.util.ArrayList;
import java.util.List;

/**
 * Hail Mary ultimate: only usable below 15% max HP. 20% accuracy, instant kill
 * on a connected hit.
 */
public class HeavenPiercingBladePassive implements Passive {

    public static final String MOVE_NAME = "Heaven Piercing Blade";
    public static final int ACCURACY = 20;
    public static final double UNLOCK_HP_FRACTION = 0.15;

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.PHYSICAL, 0, ACCURACY, 0, false,
                Status.NONE, 0, Growth.ULT_COOLDOWN_TURNS);
    }

    public static boolean isHailMaryReady(Character self) {
        return self.getStats().getCurrentHp() * 20 < self.getStats().getMaxHp() * 3;
    }

    @Override
    public List<Move> filterOwnMoves(Character self, List<Move> moves, BattleContext context) {
        if (isHailMaryReady(self)) {
            return moves;
        }
        List<Move> filtered = new ArrayList<>();
        for (Move move : moves) {
            if (!MOVE_NAME.equals(move.getName())) {
                filtered.add(move);
            }
        }
        return filtered;
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        if (move == null || !MOVE_NAME.equals(move.getName()) || target == null || target.isFainted()) {
            return;
        }
        int remaining = target.getStats().getCurrentHp() + target.getStats().getShieldHp();
        if (remaining <= 0) {
            return;
        }
        log.add(self.getName() + " slashes through the fabric of the world with "
                + MOVE_NAME + "!");
        StatusEffectResolver resolver = new StatusEffectResolver();
        resolver.applyDamageThroughShield(target, self, remaining, context, log);
        if (target.isFainted()) {
            log.add(target.getName() + " has fainted!");
            resolver.notifyFaint(target, self, move, context, log);
        }
    }
}
