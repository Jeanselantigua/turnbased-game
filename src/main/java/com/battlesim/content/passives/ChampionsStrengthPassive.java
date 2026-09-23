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
 * Each landed hit adds 12% crit chance. The bonus resets when a crit lands.
 * A kill heals the champion and living allies for 25% of the victim's max HP.
 * Uplifting Slash and Visceral Wound deal flat damage.
 */
public class ChampionsStrengthPassive implements Passive {

    public static final String UPLIFTING_SLASH = "Uplifting Slash";
    public static final String VISCERAL_WOUND = "Visceral Wound";
    public static final int UPLIFTING_DAMAGE = 35;
    public static final int UPLIFTING_PARALYZE_CHANCE = 35;
    public static final int VISCERAL_DAMAGE = 60;
    public static final int CRIT_PER_HIT = 12;
    public static final int CRIT_CAP = 100;
    /** Naked crit damage starts at 0, so the blade supplies the double-damage crit. */
    public static final int CRIT_DAMAGE_PERCENT = 100;
    public static final double KILL_HEAL_FRACTION = 0.25;

    private int bonusCrit;

    public int getBonusCrit() {
        return bonusCrit;
    }

    public static Move createUpliftingSlash() {
        return new Move(UPLIFTING_SLASH, Type.PHYSICAL, UPLIFTING_DAMAGE, 100, 0, false,
                Status.PARALYSIS, UPLIFTING_PARALYZE_CHANCE);
    }

    public static Move createVisceralWound() {
        return new Move(VISCERAL_WOUND, Type.PHYSICAL, VISCERAL_DAMAGE, 100, 0, false,
                Status.BLEED, 100);
    }

    public static int flatDamage(Move move) {
        if (move == null) {
            return -1;
        }
        if (UPLIFTING_SLASH.equals(move.getName())) {
            return UPLIFTING_DAMAGE;
        }
        if (VISCERAL_WOUND.equals(move.getName())) {
            return VISCERAL_DAMAGE;
        }
        return -1;
    }

    @Override
    public int modifyCritRate(Character self, Move move, int ratePercent) {
        if (move != null && move.targetsAllies()) {
            return ratePercent;
        }
        return ratePercent + bonusCrit;
    }

    @Override
    public int modifyCritDamage(Character self, Move move, int damagePercent) {
        if (move != null && move.targetsAllies()) {
            return damagePercent;
        }
        return damagePercent + CRIT_DAMAGE_PERCENT;
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        int flat = flatDamage(move);
        if (flat < 0) {
            return damage;
        }
        double dealt = flat;
        if (isCrit) {
            dealt *= self.critMultiplier(move);
        }
        return dealt;
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        if (move == null || move.targetsAllies()) {
            return;
        }
        if (isCrit) {
            if (bonusCrit > 0) {
                log.add(self.getName() + "'s Champion's Strength resets.");
            }
            bonusCrit = 0;
            return;
        }
        bonusCrit = Math.min(CRIT_CAP, bonusCrit + CRIT_PER_HIT);
        log.add(self.getName() + "'s Champion's Strength builds (+"
                + CRIT_PER_HIT + "% crit).");
    }

    @Override
    public void onKill(Character self, Character victim, Move move, BattleContext context, List<String> log) {
        if (self == null || self.isFainted() || victim == null || victim == self) {
            return;
        }
        int amount = (int) Math.round(victim.getStats().getMaxHp() * KILL_HEAL_FRACTION);
        if (amount <= 0) {
            return;
        }
        List<Character> party = new ArrayList<>();
        if (context != null) {
            for (Character ally : context.alliesOf(self)) {
                if (!ally.isFainted()) {
                    party.add(ally);
                }
            }
        }
        if (party.isEmpty()) {
            party.add(self);
        }
        for (Character ally : party) {
            int healed = ally.getStats().heal(amount);
            if (healed > 0) {
                log.add(ally.getName() + " recovers " + healed
                        + " HP from Champion's Strength!");
            }
        }
    }
}
