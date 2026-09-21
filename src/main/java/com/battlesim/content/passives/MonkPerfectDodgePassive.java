package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces Parry after a successful Meditate channel.
 * Dodge chance stacks on dodging, drops 5% each hit, and snaps back to base
 * after five of the monk's turns without a dodge. A dodge always counters.
 */
public class MonkPerfectDodgePassive implements Passive {

    private final MonkPerfectEnlightenmentPassive source;
    private final RandomProvider random;
    private int dodgeStacks;
    private int hitStacks;
    private int turnsWithoutDodge;

    public MonkPerfectDodgePassive(MonkPerfectEnlightenmentPassive source, RandomProvider random) {
        this.source = source;
        this.random = random;
    }

    public int getDodgeStacks() {
        return dodgeStacks;
    }

    public int getHitStacks() {
        return hitStacks;
    }

    public int getTurnsWithoutDodge() {
        return turnsWithoutDodge;
    }

    public double currentDodgeChance() {
        double chance = source.dodgeBase()
                + dodgeStacks * source.dodgeStack()
                - hitStacks * source.dodgeHitPenalty();
        return Math.max(0, Math.min(source.dodgeCap(), chance));
    }

    @Override
    public boolean rollDodge(Character self, Character attacker, Move move) {
        return random.nextDouble() < currentDodgeChance();
    }

    @Override
    public void onDodged(Character self, Character attacker, Move move,
                          BattleContext context, List<String> log) {
        turnsWithoutDodge = 0;
        dodgeStacks++;
        int percent = (int) Math.round(currentDodgeChance() * 100);
        log.add(self.getName() + " perfectly dodges! Dodge chance is now " + percent + "%.");

        int counter = Math.max(1, (int) Math.round(self.getStats().getAttack() * source.counterDamageRatio()));
        int actual = attacker.getStats().applyDamage(counter);
        log.add(self.getName() + " counters for " + actual + " damage!");
        source.applyBlessed(attacker, 1, log);
        if (attacker.isFainted()) {
            log.add(attacker.getName() + " has fainted!");
            if (context != null) {
                context.notifyFaint(attacker, self, move, log);
            } else {
                for (Passive passive : new ArrayList<>(attacker.getPassives())) {
                    passive.onFaint(attacker, self, move, null, log);
                }
            }
        }
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        if (currentDodgeChance() <= 0) {
            return;
        }
        hitStacks++;
        int percent = (int) Math.round(currentDodgeChance() * 100);
        log.add(self.getName() + " is hit! Dodge chance is now " + percent + "%.");
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        if (self == null || self.isFainted()) {
            return;
        }
        if (dodgeStacks == 0 && hitStacks == 0) {
            turnsWithoutDodge = 0;
            return;
        }
        turnsWithoutDodge++;
        if (turnsWithoutDodge >= source.dodgeResetTurns()) {
            dodgeStacks = 0;
            hitStacks = 0;
            turnsWithoutDodge = 0;
            int percent = (int) Math.round(currentDodgeChance() * 100);
            log.add(self.getName() + "'s dodge chance resets to " + percent + "%.");
        }
    }
}
