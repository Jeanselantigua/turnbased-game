package com.battlesim.content;

import com.battlesim.content.passives.CavemanAccuracyCreepPassive;
import com.battlesim.content.passives.CavemanFrenzyPassive;
import com.battlesim.content.passives.ChefChickenPassive;
import com.battlesim.content.passives.ChefSlowCookPassive;
import com.battlesim.content.passives.KnightBleedPassive;
import com.battlesim.content.passives.KnightShieldPassive;
import com.battlesim.content.passives.MonkHolySplitPassive;
import com.battlesim.content.passives.MonkParryPassive;
import com.battlesim.content.passives.MonkPerfectEnlightenmentPassive;
import com.battlesim.content.passives.RogueCritStealthPassive;
import com.battlesim.content.passives.WizardChargedPassive;
import com.battlesim.content.passives.WizardHuntPassive;
import com.battlesim.content.passives.WomanBlessingPassive;
import com.battlesim.content.passives.WomanSparkMercyPassive;
import com.battlesim.content.passives.SionExplodingShieldPassive;
import com.battlesim.content.passives.SionKillMaxHpPassive;
import com.battlesim.content.passives.SionRevivePassive;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

public class PlayableCharacters {

    public static CharacterTemplate knight() {
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        Move shieldBash = new Move("Shield Bash", Type.PHYSICAL, 35, 90, 0, false, Status.STUN, 30);

        return new CharacterTemplate("Knight", 240, 45, 40, 0, 40, 30,
                Type.PHYSICAL, List.of(slash, shieldBash),
                List.of(KnightBleedPassive::new, KnightShieldPassive::new));
    }

    public static CharacterTemplate rogue() {
        Move backstab = new Move("BackStab", Type.PHYSICAL, 40, 90, 1, false, Status.NONE, 0);
        Move crossbow = new Move("Crossbow", Type.PHYSICAL, 50, 90, 0, false, Status.NONE, 0);
        
        
        return new CharacterTemplate("Rogue", 200, 60, 20, 20, 30, 70,
                Type.ARCANE, List.of(backstab, crossbow),
                List.of(RogueCritStealthPassive::new));
    }

    public static CharacterTemplate monk() {
        Move oochie = new Move("Oochie", Type.HOLY,50, 100, 0, false, Status.NONE, 0);
        Move divinePalm = new Move("Divine Palm", Type.HOLY, 50, 95, 0, true, Status.NONE, 0);
        Move enlightenment = MonkPerfectEnlightenmentPassive.createMove();

        return new CharacterTemplate("Roeseph", 200, 45, 35, 40, 50, 30,
                Type.HOLY, List.of(oochie, divinePalm, enlightenment),
                List.of(MonkHolySplitPassive::new, MonkParryPassive::new,
                        MonkPerfectEnlightenmentPassive::new));
    }

    public static CharacterTemplate caveman() {
        Move club = new Move("Bonk", Type.PHYSICAL, 120, 40, 0, false, Status.STUN, 40);
        Move leech = new Move("Devour", Type.PHYSICAL, 40, 90, 0, false, Status.LEECH, 100);

        return new CharacterTemplate("Randy", 250, 50, 50, 0, 35, 15,
                Type.PHYSICAL, List.of(club, leech),
                List.of(CavemanFrenzyPassive::new, CavemanAccuracyCreepPassive::new));
    }
    
    public static CharacterTemplate chefromancer() {
        Move bakedbread = new Move("Night of the living Bread", Type.UNDEAD, 50, 90, 0, true, Status.CURSED, 30);
        Move siphon = new Move("Siphon", Type.UNDEAD, 35, 90, 0, true, Status.SIPHON, 100);

        return new CharacterTemplate("Chefromancer", 200, 21, 20 , 60, 65, 34,
                Type.UNDEAD, List.of(bakedbread, siphon),
                List.of(ChefChickenPassive::new, ChefSlowCookPassive::new));
    }

    public static CharacterTemplate healer() {
        Move heal = new Move("Heal", Type.HOLY, 0, 100, 0, true, Status.HEAL, 100);
        Move holySpark = new Move("Holy Spark", Type.HOLY, 55, 100, 0, true, Status.NONE, 0);
        Move barrier = new Move("Barrier", Type.HOLY, 80, 100, 0, true, Status.SHIELD, 100);

        return new CharacterTemplate("Okirik", 230, 10, 28, 60, 40, 32,
                Type.HOLY, List.of(heal, holySpark, barrier),
                List.of(WomanBlessingPassive::new, WomanSparkMercyPassive::new));
    }

    public static CharacterTemplate wizard() {
        Move bolt = new Move("Bolt", Type.LIGHTNING, 45, 95, 0, true, Status.PARALYSIS, 30);
        Move staticShock = new Move("Static", Type.LIGHTNING, 20, 90, 0, true, Status.PARALYSIS, 70);

        return new CharacterTemplate("Volt", 210, 10, 35, 60, 40, 45,
                Type.LIGHTNING, List.of(bolt, staticShock),
                List.of(WizardHuntPassive::new, WizardChargedPassive::new));
    }

    public static CharacterTemplate sion() {
        Move slam = new Move("Decamating Slam", Type.PHYSICAL, 45, 60, 0, false, Status.STUN, 100);
        Move roar = new Move("Roar of the slayer", Type.UNDEAD, 100, 100, 0, true, Status.SELF_SHIELD, 100);
        Move slow = new Move("Slow", Type.PHYSICAL, 20, 90, 0, false, Status.SLOW, 100);

        return new CharacterTemplate("Sion", 270, 25, 35, 20, 35, 15,
                Type.LIGHTNING, List.of(slam, roar, slow),
                List.of(SionRevivePassive::new, SionExplodingShieldPassive::new, SionKillMaxHpPassive::new));
    }

    public static List<CharacterTemplate> all() {
        return List.of(knight(), rogue(), monk(), caveman(), chefromancer(), healer(), wizard(), sion());
    }
}