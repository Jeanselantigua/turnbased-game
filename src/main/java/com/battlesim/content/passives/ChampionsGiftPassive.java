package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.util.RandomProvider;
import java.util.List;

/**
 * Champion's Gift: while the Mechanized Champion is below 50% max HP, they and
 * their allies gain a stacking crit chance (10% + 5% per non-crit) for 4 crits,
 * then the blade rests for {@link #COOLDOWN_TURNS} of the Champion's turns.
 */
public class ChampionsGiftPassive implements Passive {

    public static final double AWAKEN_HP_FRACTION = 0.50;
    public static final double BASE_CRIT_CHANCE = 0.10;
    public static final double CRIT_CHANCE_PER_NON_CRIT = 0.05;
    public static final int MAX_GIFT_CRITS = 4;
    public static final int COOLDOWN_TURNS = 2;
    public static final double CRIT_MULTIPLIER = 2.0;

    private final RandomProvider random;
    private boolean awakened;
    private double critChance;
    private int remainingGiftCrits;
    private int cooldownTurns;
    private boolean grantedThisHit;

    public ChampionsGiftPassive() {
        this(new RandomProvider());
    }

    public ChampionsGiftPassive(RandomProvider random) {
        this.random = random;
        this.critChance = BASE_CRIT_CHANCE;
        this.remainingGiftCrits = MAX_GIFT_CRITS;
    }

    public boolean isAwakened() {
        return awakened;
    }

    public double getCritChance() {
        return critChance;
    }

    public int getRemainingGiftCrits() {
        return remainingGiftCrits;
    }

    public int getCooldownTurns() {
        return cooldownTurns;
    }

    public static boolean isBelowHalfHealth(Character self) {
        return self.getStats().getCurrentHp() * 2 < self.getStats().getMaxHp();
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        if (cooldownTurns > 0) {
            cooldownTurns--;
        }
        tryAwaken(self, log);
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        tryAwaken(self, log);
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        tryAwaken(self, log);
    }

    @Override
    public boolean rollBonusCrit(Character self, Move move) {
        return false;
    }

    @Override
    public int modifyCritRate(Character self, Move move, int ratePercent) {
        if (!awakened || remainingGiftCrits <= 0) {
            return ratePercent;
        }
        if (move != null && !isHostileDamagingMove(move)) {
            return ratePercent;
        }
        return ratePercent + (int) Math.round(critChance * 100);
    }

    @Override
    public boolean rollBonusCritForAlly(Character self, Character ally, Move move) {
        return rollGiftCrit(move);
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (isCrit && awakened && remainingGiftCrits > 0 && isHostileDamagingMove(move)) {
            log.add(self.getName() + " crits through Champion's Gift!");
            return damage * CRIT_MULTIPLIER;
        }
        return damage;
    }

    @Override
    public double modifyOutgoingDamageGrantedToAlly(Character self, Character ally, Character target,
                                                     Move move, double damage, boolean isCrit,
                                                     List<String> log) {
        return applyGiftCritDamage(ally, damage, log);
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        noteOwnAttack(move, isCrit, log);
    }

    @Override
    public void onAllyAttackConnected(Character self, Character attacker, Character target,
                                       Move move, int damageDealt, boolean isCrit, boolean blocked,
                                       List<String> log, BattleContext context) {
        noteAttackResult(move, log);
    }

    private boolean rollGiftCrit(Move move) {
        grantedThisHit = false;
        if (!awakened || remainingGiftCrits <= 0 || !isHostileDamagingMove(move)) {
            return false;
        }
        if (random.nextDouble() < critChance) {
            grantedThisHit = true;
            return true;
        }
        return false;
    }

    private double applyGiftCritDamage(Character attacker, double damage, List<String> log) {
        if (!grantedThisHit) {
            return damage;
        }
        log.add(attacker.getName() + " crits through Champion's Gift!");
        return damage * CRIT_MULTIPLIER;
    }

    private void noteAttackResult(Move move, List<String> log) {
        if (!awakened || !isHostileDamagingMove(move)) {
            grantedThisHit = false;
            return;
        }
        if (grantedThisHit) {
            remainingGiftCrits--;
            grantedThisHit = false;
            if (remainingGiftCrits <= 0) {
                awakened = false;
                cooldownTurns = COOLDOWN_TURNS;
                critChance = BASE_CRIT_CHANCE;
                remainingGiftCrits = MAX_GIFT_CRITS;
                log.add("Champion's Gift fades. The blade rests.");
            }
        } else {
            critChance += CRIT_CHANCE_PER_NON_CRIT;
        }
    }

    private void noteOwnAttack(Move move, boolean isCrit, List<String> log) {
        if (!awakened || !isHostileDamagingMove(move)) {
            return;
        }
        if (isCrit) {
            remainingGiftCrits--;
            if (remainingGiftCrits <= 0) {
                awakened = false;
                cooldownTurns = COOLDOWN_TURNS;
                critChance = BASE_CRIT_CHANCE;
                remainingGiftCrits = MAX_GIFT_CRITS;
                log.add("Champion's Gift fades. The blade rests.");
            }
        } else {
            critChance += CRIT_CHANCE_PER_NON_CRIT;
        }
    }

    private void tryAwaken(Character self, List<String> log) {
        if (awakened || cooldownTurns > 0 || self.isFainted() || !isBelowHalfHealth(self)) {
            return;
        }
        awakened = true;
        critChance = BASE_CRIT_CHANCE;
        remainingGiftCrits = MAX_GIFT_CRITS;
        log.add(self.getName() + "'s blade comes to life, radiating Champion's Gift!");
    }

    private static boolean isHostileDamagingMove(Move move) {
        return move != null && !move.targetsAllies() && move.getPower() > 0;
    }
}
