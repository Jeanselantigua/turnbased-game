package com.battlesim.content;

import com.battlesim.model.*;
import com.battlesim.model.Character;

import java.util.List;

public class PlayableCharacters {

    public static CharacterTemplate rogue() {
    	
    	/*
         * Passive 1 (Crit Chance): 30% for a crit, this deals double dmg and grants stealth
         * 
         * Passive 2 (Stealth): After a succesful crit go stealth and become untargetable until your next attack and deal double dmg.
         */
    	
        Move backstab = new Move("Assasinate", Type.PHYSICAL, 60, 80, 1, false, Status.NONE, 0);
        Move crossbow = new Move("Crossbow", Type.PHYSICAL, 90, 90, 0, false, Status.NONE, 0);
        return new CharacterTemplate("Rogue", 60, 45, 10, 20, 10, 100,
                Type.ARCANE, List.of(backstab, crossbow));
    }
    

    public static CharacterTemplate knight() {
    	/*
         * Passive 1 (Bleed) : 20% chance for Physical attacks to inflict bleed equal to 20% of you attack
         * 
         * Passive 2 (Shield) : 15% chance to block all forms of incoming dmg
         */
    	
        Move slash = new Move("Slash", Type.PHYSICAL, 80, 90, 0, false, Status.BLEED, 30);
        return new CharacterTemplate("Knight", 120, 45, 40, 10, 15, 30,
                Type.PHYSICAL, List.of(slash));
    }

    
    
    // add monk(), caveman(), and every future character here
    
//    Stats monkStats = new Stats(90, 40, 20, 40, 40, 30); // 260 BST
//    Character monk = new Character("Roeseph", monkStats, Type.HOLY, List.of(staff));
//    
//    /*
//     * Passive 1 (Holy): Each hit deals a % of the total damage as magic dmg
//     * 
//     * Passive 2 (Parry): 15% chance to negate all physical dmg and reflect half of it back onto the attacker
//     */
//    
//    Stats cavemanStats = new Stats(100, 70, 50, 0, 35, 5); // 260 BST
//    Character caveman = new Character("Randy", cavemanStats, Type.PHYSICAL, List.of(club,flamingClub));
//    /*
//     * Passive 1 (Frenzy): Character can't increase speed ever but after taking gain +5 attack +10 def & magic def.
//     * 
//     * Passive 2 (Retard enlightenment): After every missed attack gain +5 acc until hit
//     */
    
}