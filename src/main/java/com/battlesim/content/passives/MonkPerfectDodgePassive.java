package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces Parry after a successful Perfect Enlightenment channel.
 * Dodge chance stacks on dodging attacks or debuffs; a dodge always counters.
 */
public class MonkPerfectDodgePassive implements Passive {

    private final MonkPerfectEnlightenmentPassive source;
    private final RandomProvider random;
    private int dodgeStacks;

    public MonkPerfectDodgePassive(MonkPerfectEnlightenmentPassive source, RandomProvider random) {
        this.source = source;
        this.random = random;
    }

    public int getDodgeStacks() {
        return dodgeStacks;
    }

    public double currentDodgeChance() {
        return Math.min(source.dodgeCap(), source.dodgeBase() + dodgeStacks * source.dodgeStack());
    }

    @Override
    public boolean rollDodge(Character self, Character attacker, Move move) {
        return random.nextDouble() < currentDodgeChance();
    }

    @Override
    public void onDodged(Character self, Character attacker, Move move,
                          BattleContext context, List<String> log) {
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
}
