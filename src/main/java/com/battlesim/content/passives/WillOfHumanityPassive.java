package com.battlesim.content.passives;

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
 * On death, once the ult is learned, the champion returns at full HP.
 * They take 70% less damage for four of their turns and deal 32% less
 * damage for the rest of the run. The ult is not cast; death triggers it.
 */
public class WillOfHumanityPassive implements Passive {

    public static final String MOVE_NAME = "The Will of Humanity";
    public static final int GUARD_TURNS = 4;
    public static final double INCOMING_MULTIPLIER = 0.30;
    public static final double OUTGOING_MULTIPLIER = 0.68;

    private boolean bladeSpent;
    private int guardTurns;

    public boolean isBladeSpent() {
        return bladeSpent;
    }

    public int getGuardTurns() {
        return guardTurns;
    }

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.PHYSICAL, 0, 100, 0, false,
                Status.NONE, 0, Growth.ULT_COOLDOWN_TURNS);
    }

    @Override
    public List<Move> filterOwnMoves(Character self, List<Move> moves, BattleContext context) {
        List<Move> filtered = new ArrayList<>();
        for (Move move : moves) {
            if (!MOVE_NAME.equals(move.getName())) {
                filtered.add(move);
            }
        }
        return filtered;
    }

    @Override
    public void onFaint(Character self, Character killer, Move move, BattleContext context, List<String> log) {
        if (bladeSpent || self == null || !self.isFainted() || !self.knowsMove(MOVE_NAME)) {
            return;
        }
        self.getStats().restoreFully();
        bladeSpent = true;
        guardTurns = GUARD_TURNS;
        log.add(self.getName() + " answers with The Will of Humanity!");
        log.add("The blade is spent. Full health, 70% less damage taken for "
                + GUARD_TURNS + " turns, and 32% weaker strikes for the rest of the run.");
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        if (guardTurns <= 0) {
            return;
        }
        guardTurns--;
        if (guardTurns == 0) {
            log.add(self.getName() + "'s second wind fades.");
        }
    }

    @Override
    public double modifyIncomingDamage(Character self, Character attacker, Move move,
                                        double damage, List<String> log) {
        if (guardTurns <= 0) {
            return damage;
        }
        return damage * INCOMING_MULTIPLIER;
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (!bladeSpent) {
            return damage;
        }
        return damage * OUTGOING_MULTIPLIER;
    }
}
