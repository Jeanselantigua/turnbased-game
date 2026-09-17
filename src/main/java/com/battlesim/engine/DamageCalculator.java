package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.util.RandomProvider;

/**
 * Computes how much damage a Move deals from an attacker to a defender.
 * TypeChart and RandomProvider are injected (passed in) rather than
 * created here, which is what makes this class easy to unit test —
 * a test can pass a RandomProvider that always returns the same value.
 */
public class DamageCalculator {

    private final TypeChart typeChart;
    private final RandomProvider randomProvider;

    public DamageCalculator(TypeChart typeChart, RandomProvider randomProvider) {
        this.typeChart = typeChart;
        this.randomProvider = randomProvider;
    }

    public int calculateDamage(Character attacker, Character defender, Move move) {
        if (move.getPower() == 0) {
            return 0; // pure status/utility moves deal no damage
        }

        int offenseStat = move.isMagic()
                ? attacker.getStats().getMagicAttack()
                : attacker.getStats().getAttack();
        int defenseStat = move.isMagic()
                ? defender.getStats().getMagicDefense()
                : defender.getStats().getDefense();

        double base = ((double) move.getPower() * offenseStat) / Math.max(1, defenseStat);

        double stab = (move.getType() == attacker.getAffinity()) ? 1.5 : 1.0;
        double typeMultiplier = typeChart.getMultiplier(move.getType(), defender.getAffinity());
        double variance = randomProvider.nextInt(85, 100) / 100.0; // 85%-100% roll

        double total = base * stab * typeMultiplier * variance;

        return (int) Math.round(total);
    }
}
