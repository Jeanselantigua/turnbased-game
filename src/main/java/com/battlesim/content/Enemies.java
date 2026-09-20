package com.battlesim.content;

import com.battlesim.content.passives.DragonPhasePassive;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.EnemyTemplate;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;
import java.util.Set;

/**
 * Enemy roster, same idea as {@link PlayableCharacters}.
 * Replace the Dummy entries with real normals, elites, and bosses.
 */
public class Enemies {

    public static EnemyTemplate goblin() {
        Move jab = new Move("Jab", Type.PHYSICAL, 20, 100, 0, false, Status.NONE, 0);
        CharacterTemplate body = new CharacterTemplate(
                "goblin", 80, 20, 15, 0, 10, 25,
                Type.MONSTER, List.of(jab));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate wolf() {
        
        Move bite = new Move("Bite", Type.PHYSICAL, 20, 100, 0, false, Status.NONE, 0);
        CharacterTemplate body = new CharacterTemplate(
                "Wolf", 100, 30, 20, 0, 15, 30,
                Type.PHYSICAL, List.of(bite));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }
    
    public static EnemyTemplate spider() {
        Move web = new Move("Web", Type.PHYSICAL, 20, 100, 0, false, Status.SLOW, 100);
        Move sting = new Move("Poison Strike", Type.PHYSICAL, 20, 100, 0, false, Status.POISON, 80);
        CharacterTemplate body = new CharacterTemplate(
                "Spider", 100, 30, 20, 0, 15, 30,
                Type.PHYSICAL, List.of(web, sting));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate iceSlime() {
        Move bounce = new Move("Bounce", Type.ICE, 20, 100, 0, false, Status.SLOW, 75);
        CharacterTemplate body = new CharacterTemplate(
                "Ice Slime", 50, 20, 25, 0, 10, 15,
                Type.ICE, List.of(bounce));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate fireSlime() {
        Move bounce = new Move("Bounce", Type.FIRE, 20, 100, 0, false, Status.BURN, 75);
        CharacterTemplate body = new CharacterTemplate(
                "Magma Slime", 50, 20, 25, 0, 10, 15,
                Type.FIRE, List.of(bounce));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate earthSlime() {
        Move bounce = new Move("Bounce", Type.EARTH, 20, 100, 0, false, Status.POISON, 75);
        CharacterTemplate body = new CharacterTemplate(
                "Earth Slime", 50, 20, 25, 0, 10, 15,
                Type.EARTH, List.of(bounce));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate lightningSlime() {
        Move bounce = new Move("Bounce", Type.LIGHTNING, 20, 100, 0, false, Status.PARALYSIS, 75);
        CharacterTemplate body = new CharacterTemplate(
                "Lightning Slime", 50, 20, 25, 0, 10, 15,
                Type.LIGHTNING, List.of(bounce));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate swordskeleton() {
        Move slash = new Move("Slash", Type.PHYSICAL, 45, 90, 0, false, Status.BLEED, 20);
        Move sheild = new Move("Shield", Type.PHYSICAL, 35, 100, 1, false, Status.SELF_SHIELD, 100);
        CharacterTemplate body = new CharacterTemplate(
                "Skeleton", 160, 45, 25, 0, 20, 25,
                Type.UNDEAD, List.of(slash, sheild));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }
    
    public static EnemyTemplate bowskeleton() {
        Move arrow = new Move("Bow Shot", Type.PHYSICAL, 45, 90, 0, false, Status.NONE, 20);
        Move poisonarrow = new Move("Poison Arrow", Type.PHYSICAL, 35, 90, 0, false, Status.POISON, 20);
        Move shockarrow = new Move("Shock Arrow", Type.PHYSICAL, 35, 90, 0, false, Status.PARALYSIS, 20);

        CharacterTemplate body = new CharacterTemplate(
                "Skeleton", 120, 45, 25, 0, 20, 35,
                Type.UNDEAD, List.of(arrow, poisonarrow, shockarrow));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    public static EnemyTemplate ghoul() {
        Move bite = new Move("Bite", Type.UNDEAD, 35, 90, 0, false, Status.CURSED, 20);
        
        CharacterTemplate body = new CharacterTemplate(
                "Ghoul", 160, 35, 25, 10, 20, 30,
                Type.UNDEAD, List.of(bite));
        return new EnemyTemplate(body, EnemyRank.NORMAL);
    }

    //Elite enemies
    public static EnemyTemplate eliteDummy() {
        Move smash = new Move("Smash", Type.PHYSICAL, 45, 90, 0, false, Status.STUN, 20);
        Move slam = new Move("Slam", Type.PHYSICAL, 35, 100, 0, false, Status.NONE, 0);
        CharacterTemplate body = new CharacterTemplate(
                "Elite Dummy", 160, 35, 25, 10, 20, 30,
                Type.PHYSICAL, List.of(smash, slam));
        return new EnemyTemplate(body, EnemyRank.ELITE);
    }

    public static EnemyTemplate orc() {
        Move slam = new Move("Club Slam", Type.PHYSICAL, 45, 100, 0, false, Status.NONE, 0);
        CharacterTemplate body = new CharacterTemplate(
                "Orc", 250, 35, 25, 10, 20, 30,
                Type.MONSTER, List.of(slam));
        return new EnemyTemplate(body, EnemyRank.ELITE);
    }

    public static EnemyTemplate greaterFireSpirit(){
        Move fireball = new Move("Fireball", Type.FIRE, 45, 90, 0, true, Status.BURN, 20);
        Move fireblast = new Move("Fire Blast", Type.FIRE, 35, 100, 0, true, Status.BURN, 30);
        CharacterTemplate body = new CharacterTemplate(
                "Greater Fire Spirit", 250, 10, 25, 35, 20, 30,
                Type.FIRE, List.of(fireball, fireblast));
        return new EnemyTemplate(body, EnemyRank.ELITE);
    }

    //Boss enemies
    public static EnemyTemplate bossDummy() {
        Move crush = new Move("Crush", Type.PHYSICAL, 60, 90, 0, false, Status.NONE, 0);
        Move roar = new Move("Roar", Type.PHYSICAL, 20, 100, 0, false, Status.STUN, 30);
        CharacterTemplate body = new CharacterTemplate(
                "Boss Dummy", 600, 50, 35, 20, 30, 28,
                Type.PHYSICAL, List.of(crush, roar));
        // extraActions=1 and STUN immunity are data only until EnemyTemplate.createInstance wires them
        return new EnemyTemplate(body, EnemyRank.BOSS, Set.of(Status.STUN), 1);
    }

    public static EnemyTemplate hulk() {
        Move crush = new Move("Smash", Type.PHYSICAL, 60, 90, 0, false, Status.NONE, 0);
        Move roar = new Move("Roar", Type.PHYSICAL, 20, 100, 0, false, Status.STUN, 30);
        CharacterTemplate body = new CharacterTemplate(
                "Hulk", 600, 50, 45, 0, 40, 35,
                Type.PHYSICAL, List.of(crush, roar));
        // extraActions=1 and STUN immunity are data only until EnemyTemplate.createInstance wires them
        return new EnemyTemplate(body, EnemyRank.BOSS, Set.of(Status.STUN), 1);
    }

    public static EnemyTemplate dragon() {
        Move breath = new Move("Flame Breath", Type.FIRE, 60, 90, 0, true, Status.BURN, 70);
        Move claw = new Move("Claw", Type.PHYSICAL, 40, 100, 0, false, Status.BLEED, 20);
        Move roar = new Move("Roar", Type.PHYSICAL, 20, 100, 0, false, Status.STUN, 30);
        CharacterTemplate body = new CharacterTemplate(
                "Fire Dragon", 600, 30, 25, 50, 40, 35,
                Type.FIRE, List.of(breath, roar, claw),
                List.of(DragonPhasePassive::new));
        return new EnemyTemplate(body, EnemyRank.BOSS, Set.of(Status.BURN), 0);
    }

    public static EnemyTemplate goblinKing() {
        Move smash = new Move("Smash", Type.PHYSICAL, 45, 90, 0, false, Status.STUN, 20);
        // Potential Moves:
        // - Summon Goblin


        CharacterTemplate body = new CharacterTemplate(
                "Goblin King", 600, 45, 55, 10, 50, 30,
                Type.MONSTER, List.of(smash));
        return new EnemyTemplate(body, EnemyRank.BOSS);
    }

    public static EnemyTemplate fastestManAlive() {
        Move punch = new Move("Punch", Type.PHYSICAL, 25, 100, 0, false, Status.NONE, 0);
        Move suprise = new Move("Sucker Punch", Type.PHYSICAL, 15, 100, 0, false, Status.STUN, 60);
        
        CharacterTemplate body = new CharacterTemplate(
                "Fastest Man Alive", 500, 35, 25, 0, 20, 100,
                Type.PHYSICAL, List.of(punch, suprise));
        return new EnemyTemplate(body, EnemyRank.BOSS, Set.of(Status.SLOW), 0);
    }

    public static List<EnemyTemplate> normals() {
        return List.of(goblin());
    }

    public static List<EnemyTemplate> elites() {
        return List.of(eliteDummy());
    }

    public static List<EnemyTemplate> bosses() {
        return List.of(bossDummy());
    }

    public static List<EnemyTemplate> all() {
        return List.of(
            goblin(), 
            wolf(), 
            spider(), 
            iceSlime(), 
            fireSlime(), 
            earthSlime(), 
            lightningSlime(), 
            swordskeleton(), 
            bowskeleton(), 
            ghoul(), 
            eliteDummy(), 
            orc(),
            greaterFireSpirit(),
            bossDummy(), 
            hulk(),
            dragon(),
            goblinKing(),
            fastestManAlive()
        );
    }
}
