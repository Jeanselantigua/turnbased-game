package com.battlesim.model;

import java.util.List;

public interface Passive {

    default boolean isUntargetable(Character self) {
        return false;
    }

    default int modifyAccuracy(Character self, Move move, int baseAccuracy) {
        return baseAccuracy;
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

    default void onAttackMissed(Character self, Move move, List<String> log) {
    }

    default void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
    }
}