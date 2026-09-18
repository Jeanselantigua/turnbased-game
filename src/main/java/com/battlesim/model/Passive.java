package com.battlesim.model;

import java.util.List;

public interface Passive {

    default boolean isUntargetable(Character self) {
        return false;
    }

    default int modifyAccuracy(Character self, Move move, int baseAccuracy) {
        return baseAccuracy;
    }

    default int modifyAccuracy(Character self, Character target, Move move, int baseAccuracy) {
        return modifyAccuracy(self, move, baseAccuracy);
    }

    default boolean isStealthed(Character self) {
        return false;
    }

    default void onHealed(Character self, int amount, List<String> log) {
    }

    default void onAllyHealed(Character self, Character target, int amount, List<String> log) {
    }

    default double modifyIncomingDamageToAlly(Character self, Character ally, Character attacker,
                                               Move move, double damage, List<String> log) {
        return damage;
    }

    /** Defender-side accuracy, e.g. stealth evasion. Ally-targeted moves skip this. */
    default int modifyIncomingAccuracy(Character self, Character attacker, Move move, int accuracy) {
        return accuracy;
    }

    default boolean rollBonusCrit(Character self, Move move) {
        return false;
    }

    default double modifyOutgoingDamage(Character self, Character target, Move move,
                                         double damage, boolean isCrit, List<String> log) {
        return damage;
    }

    default double modifyIncomingDamage(Character self, Character attacker, Move move,
                                         double damage, List<String> log) {
        return damage;
    }

    default void onHitLanded(Character self, Character target, Move move,
                              int damageDealt, boolean isCrit, List<String> log) {
    }

    default void onHitLanded(Character self, Character target, Move move,
                              int damageDealt, boolean isCrit, List<String> log,
                              BattleContext context) {
        onHitLanded(self, target, move, damageDealt, isCrit, log);
    }

    default void onActionResolved(Character self, Move move, List<Character> targets,
                                   BattleContext context, List<String> log) {
    }

    default void onAttackMissed(Character self, Move move, List<String> log) {
    }

    default void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
    }
}
