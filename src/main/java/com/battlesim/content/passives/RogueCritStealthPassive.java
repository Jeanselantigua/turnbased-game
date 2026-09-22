package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

public class RogueCritStealthPassive implements Passive {

    public static final String AMBUSH_NAME = "Ambush";
    public static final int AMBUSH_POWER = 40;
    public static final int AMBUSH_ACCURACY = 95;
    public static final double CRIT_CHANCE = 0.15;
    public static final double AMBUSH_CRIT_CHANCE = 0.45;
    public static final int CRIT_DAMAGE_PERCENT = 100;
    private static final double STEALTH_MULTIPLIER = 1.5;
    private static final int STEALTH_EVASION = 35;

    private final RandomProvider random;
    private boolean stealthed = false;

    public RogueCritStealthPassive() {
        this(new RandomProvider());
    }

    public RogueCritStealthPassive(RandomProvider random) {
        this.random = random;
    }

    public static Move createAmbush() {
        return new Move(AMBUSH_NAME, Type.PHYSICAL, AMBUSH_POWER, AMBUSH_ACCURACY, 1, false,
                Status.NONE, 0);
    }

    @Override
    public boolean isStealthed(Character self) {
        return stealthed;
    }

    void setStealthed(boolean stealthed) {
        this.stealthed = stealthed;
    }

    @Override
    public int modifyCritRate(Character self, Move move, int ratePercent) {
        int bonus = (int) Math.round((isAmbush(move) ? AMBUSH_CRIT_CHANCE : CRIT_CHANCE) * 100);
        return ratePercent + bonus;
    }

    @Override
    public int modifyCritDamage(Character self, Move move, int damagePercent) {
        return damagePercent + CRIT_DAMAGE_PERCENT;
    }

    @Override
    public List<Move> filterOwnMoves(Character self, List<Move> moves, BattleContext context) {
        if (stealthed) {
            return moves;
        }
        List<Move> filtered = new ArrayList<>();
        for (Move move : moves) {
            if (!isAmbush(move)) {
                filtered.add(move);
            }
        }
        return filtered;
    }

    @Override
    public int modifyIncomingAccuracy(Character self, Character attacker, Move move, int accuracy) {
        if (!stealthed) {
            return accuracy;
        }
        return accuracy - STEALTH_EVASION;
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (stealthed) {
            log.add(self.getName() + " strikes from the shadows!");
            return damage * STEALTH_MULTIPLIER;
        }
        return damage;
    }

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log) {
        if (stealthed && damageDealt > 0) {
            stealthed = false;
        }
        if (isCrit) {
            stealthed = true;
            log.add(self.getName() + " vanishes into the shadows!");
        }
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        if (stealthed && damageTaken > 0) {
            stealthed = false;
            log.add(self.getName() + " is forced out of the shadows!");
        }
    }

    private static boolean isAmbush(Move move) {
        return move != null && AMBUSH_NAME.equals(move.getName());
    }
}
