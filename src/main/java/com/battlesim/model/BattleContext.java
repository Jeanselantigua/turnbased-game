package com.battlesim.model;

import java.util.ArrayList;
import java.util.List;

/** Live battle hooks for passives that summon, inspect allies, or buff teammates. */
public interface BattleContext {

    List<Character> alliesOf(Character character);

    List<Character> enemiesOf(Character character);

    void summonAlly(Character summoner, Character summon, List<String> log);

    /**
     * Adds a teammate that is not bound to {@code allyOf}. Unlike {@link #summonAlly},
     * the new fighter stays if {@code allyOf} is already fainted.
     */
    default void addAlly(Character allyOf, Character newMember, List<String> log) {
        summonAlly(allyOf, newMember, log);
        newMember.setSummoner(null);
    }

    default void notifyFaint(Character fainted, Character killer, Move move, List<String> log) {
        for (Passive passive : new ArrayList<>(fainted.getPassives())) {
            passive.onFaint(fainted, killer, move, this, log);
        }
    }
}
