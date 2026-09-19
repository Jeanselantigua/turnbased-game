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

    /** True while the owner should skip their action (e.g. channeling). */
    default boolean skipsOwnAction(Character self) {
        return false;
    }

    default void onActionSkipped(Character self, BattleContext context, List<String> log) {
    }

    default void onTurnStart(Character self, BattleContext context, List<String> log) {
    }

    /** Called when a status is successfully applied to this character. */
    default void onStatusReceived(Character self, Status status, Character source, List<String> log) {
    }

    /**
     * Called for every connected hostile hit, including 0-damage and blocked ones.
     * Existing {@link #onHitLanded} still fires only when damage is dealt.
     */
    default void onAttackConnected(Character self, Character target, Move move,
                                    int damageDealt, boolean isCrit, boolean blocked,
                                    List<String> log, BattleContext context) {
    }

    /** Separate dodge roll after a hit is confirmed. */
    default boolean rollDodge(Character self, Character attacker, Move move) {
        return false;
    }

    default void onDodged(Character self, Character attacker, Move move,
                           BattleContext context, List<String> log) {
    }

    default List<Move> filterOwnMoves(Character self, List<Move> moves, BattleContext context) {
        return moves;
    }

    /** Restrict what an opponent may use while this character is in a special state. */
    default List<Move> restrictOpponentMoves(Character self, Character opponent,
                                              List<Move> moves, BattleContext context) {
        return moves;
    }

    default boolean forcesTeamLoss(Character self, BattleContext context) {
        return false;
    }
}
