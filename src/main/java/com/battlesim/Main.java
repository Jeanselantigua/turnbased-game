package com.battlesim;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

public class Main {
    public static void main(String[] args) {
    	
    	// Knight
    	Move slash = new Move("Slash", Type.PHYSICAL, 80, 90, 0, false, Status.BLEED, 30);
    	
    	// Rogue
    	Move backstab = new Move ("Assasinate", Type.PHYSICAL, 60, 80, 1, false, Status.NONE, 0 );
        Move crossbow = new Move ("Crossbow", Type.PHYSICAL, 90, 90, 0, false, Status.NONE, 0);
        Move dart = new Move ("Shock Dart", Type.PHYSICAL, 40, 100, 0, false, Status.PARALYSIS, 75);
        
        // Monk
        Move staff = new Move ("Oochie", Type.PHYSICAL, 70, 95,0, false, Status.STUN, 35 );
        
        // Caveman
        Move club = new Move("Bonk", Type.PHYSICAL, 150, 35, 0, false, Status.STUN, 65);
        Move flamingClub = new Move("Fire Bonk", Type.PHYSICAL, 150, 35, 0, false, Status.BURN, 15);
        
        // Universal Moves
        Move fireball = new Move("Fireball", Type.FIRE, 80, 90, 0, true, Status.BURN, 10);
        Move heal = new Move("Heal", Type.HOLY, 0, 100, 0, true, Status.NONE, 0);
    	
    	/* stats go maxHP, attack, defense, magicAttack, magicDefense, speed
    	   moves go name, type, power, acc, prio, and isMagic
    	   character goes name, stats, affinity move list
    	*/
    	
        Stats knightStats = new Stats(120, 45, 40, 10, 15, 30); // 260 BST
        Character knight = new Character("Knight", knightStats, Type.PHYSICAL, List.of(slash));
        /*
         * Passive 1 (Bleed) : 20% chance for Physical attacks to inflict bleed equal to 20% of you attack
         * 
         * Passive 2 (Shield) : 15% chance to block all forms of incoming dmg
         */
        
        Stats rogueStats = new Stats(60, 45, 10, 20, 10, 100); // 260 BST
        Character rogue = new Character("Rogue", rogueStats, Type.ARCANE, List.of(backstab, crossbow, dart));       
        /*
         * Passive 1 (Crit Chance): 30% for a crit, this deals double dmg and grants stealth
         * 
         * Passive 2 (Stealth): After a succesful crit go stealth and become untargetable until your next attack and deal double dmg.
         */
        
        Stats monkStats = new Stats(90, 40, 20, 40, 40, 30); // 260 BST
        Character monk = new Character("Roeseph", monkStats, Type.HOLY, List.of(staff));
        
        /*
         * Passive 1 (Holy): Each hit deals a % of the total damage as magic dmg
         * 
         * Passive 2 (Parry): 15% chance to negate all physical dmg and reflect half of it back onto the attacker
         */
        
        Stats cavemanStats = new Stats(100, 70, 50, 0, 35, 5); // 260 BST
        Character caveman = new Character("Randy", cavemanStats, Type.PHYSICAL, List.of(club,flamingClub));
        /*
         * Passive 1 (Frenzy): Character can't increase speed ever but after taking gain +5 attack +10 def & magic def.
         * 
         * Passive 2 (Retard enlightenment): After every missed attack gain +5 acc until hit
         */
        

        Move chosen = knight.getMoveByName("Slash");
        System.out.println(knight.getName() + " uses " + chosen.getName() + "!");
    }
}