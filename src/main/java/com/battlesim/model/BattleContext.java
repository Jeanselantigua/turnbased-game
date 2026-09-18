package com.battlesim.model;

import java.util.List;

/** Live battle hooks for passives that summon, inspect allies, or buff teammates. */
public interface BattleContext {

    List<Character> alliesOf(Character character);

    List<Character> enemiesOf(Character character);

    void summonAlly(Character summoner, Character summon, List<String> log);
}
