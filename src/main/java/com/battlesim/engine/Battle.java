package com.battlesim.engine;

import com.battlesim.model.Character;

/**
 * Loops turn resolution between two Characters until one faints.
 * TODO: implement the battle loop once TurnResolver works.
 */
public class Battle {

    private final Character combatantOne;
    private final Character combatantTwo;

    public Battle(Character combatantOne, Character combatantTwo) {
        this.combatantOne = combatantOne;
        this.combatantTwo = combatantTwo;
    }

    public void run() {
        // TODO: implement
    }
}
