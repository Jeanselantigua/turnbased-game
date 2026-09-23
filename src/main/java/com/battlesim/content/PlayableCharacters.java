package com.battlesim.content;

import com.battlesim.content.passives.CavemanAccuracyCreepPassive;
import com.battlesim.content.passives.DualSwordsmanDoubleHitPassive;
import com.battlesim.content.passives.DualSwordsmanStatusRerollPassive;
import com.battlesim.content.passives.CavemanFrenzyPassive;
import com.battlesim.content.passives.ChefChickenPassive;
import com.battlesim.content.passives.ChefSlowCookPassive;
import com.battlesim.content.passives.ChampionsStrengthPassive;
import com.battlesim.content.passives.PridefulTauntPassive;
import com.battlesim.content.passives.WellOiledRagePassive;
import com.battlesim.content.passives.WillOfHumanityPassive;
import com.battlesim.content.passives.MonkDivinePalmPassive;
import com.battlesim.content.passives.MonkHolySplitPassive;
import com.battlesim.content.passives.MonkMasterOfAnyArtPassive;
import com.battlesim.content.passives.MonkParryPassive;
import com.battlesim.content.passives.MonkPerfectEnlightenmentPassive;
import com.battlesim.content.passives.ElectricWhirlwindPassive;
import com.battlesim.content.passives.RogueCritStealthPassive;
import com.battlesim.content.passives.RogueWoundPassive;
import com.battlesim.content.passives.RogueMomentumPassive;
import com.battlesim.content.passives.WizardChargedPassive;
import com.battlesim.content.passives.WizardHuntPassive;
import com.battlesim.content.passives.WizardStormPassive;
import com.battlesim.content.passives.WomanBlessingPassive;
import com.battlesim.content.passives.WomanSparkMercyPassive;
import com.battlesim.content.passives.SionExplodingShieldPassive;
import com.battlesim.content.passives.SionKillMaxHpPassive;
import com.battlesim.content.passives.SionRevivePassive;
import com.battlesim.content.passives.SionSlamPassive;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Move;
import com.battlesim.model.MoveKit;
import com.battlesim.model.StatKind;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import java.util.List;

public class PlayableCharacters {

    public static CharacterTemplate knight() {
        Move upliftingSlash = ChampionsStrengthPassive.createUpliftingSlash();
        Move pridefulTaunt = PridefulTauntPassive.createMove();
        Move visceralWound = ChampionsStrengthPassive.createVisceralWound();
        Move willOfHumanity = WillOfHumanityPassive.createMove();

        // Passive order: flat damage, then rage, then the spent-blade penalty.
        return new CharacterTemplate("Mechanized Champion", 120, 30, 20, 0, -10, 20,
                Type.PHYSICAL, MoveKit.ladder(upliftingSlash, pridefulTaunt, visceralWound, willOfHumanity),
                List.of(ChampionsStrengthPassive::new, WellOiledRagePassive::new,
                        PridefulTauntPassive::new, WillOfHumanityPassive::new),
                List.of(StatKind.HP, StatKind.DEFENSE));
    }

    public static CharacterTemplate rogue() {
        Move backstab = new Move("BackStab", Type.PHYSICAL, 30, 90, 1, false, Status.NONE, 0);
        Move ambush = RogueCritStealthPassive.createAmbush();
        Move maim = RogueWoundPassive.createMaim();
        Move assassinate = RogueWoundPassive.createAssassinate();

        return new CharacterTemplate("Rogue", 100, 30, 10, 10, 15, 35,
                Type.ARCANE, MoveKit.ladder(backstab, ambush, maim, assassinate),
                List.of(RogueCritStealthPassive::new, RogueMomentumPassive::new, RogueWoundPassive::new),
                List.of(StatKind.SPEED, StatKind.ATTACK));
    }

    public static CharacterTemplate monk() {
        Move oochie = new Move(MonkPerfectEnlightenmentPassive.OOCHIE_NAME, Type.HOLY, 60, 100, 0, false, Status.NONE, 0);
        Move divinePalm = new Move(MonkPerfectEnlightenmentPassive.DIVINE_PALM_NAME, Type.HOLY, 40, 95, 0, true, Status.NONE, 0);
        Move meditate = MonkPerfectEnlightenmentPassive.createMove();
        Move master = MonkMasterOfAnyArtPassive.createMove();

        return new CharacterTemplate("Roeseph", 84, 23, 18, 20, 25, 15,
                Type.HOLY, MoveKit.ladder(oochie, meditate, divinePalm, master),
                List.of(MonkHolySplitPassive::new, MonkParryPassive::new,
                        MonkPerfectEnlightenmentPassive::new, MonkMasterOfAnyArtPassive::new,
                        MonkDivinePalmPassive::new),
                List.of(StatKind.ATTACK, StatKind.MAGIC_ATTACK));
    }

    public static CharacterTemplate caveman() {
        Move club = new Move("Bonk", Type.PHYSICAL, 120, 60, 0, false, Status.STUN, 40);
        Move leech = new Move("Devour", Type.PHYSICAL, 40, 90, 0, false, Status.LEECH, 100);
        Move rampage = new Move("Rampage", Type.PHYSICAL, 55, 80, 0, false, Status.NONE, 0);
        Move meteorClub = new Move("Meteor Club", Type.PHYSICAL, 140, 70, 0, false, Status.STUN, 60,
                Growth.ULT_COOLDOWN_TURNS);

        return new CharacterTemplate("Randy", 94, 25, 35, 0, 20, 10,
                Type.PHYSICAL, MoveKit.ladder(club, leech, rampage, meteorClub),
                List.of(CavemanFrenzyPassive::new, CavemanAccuracyCreepPassive::new),
                List.of(StatKind.HP, StatKind.ATTACK));
    }
    
    public static CharacterTemplate chefromancer() {
        Move bakedbread = new Move("Night of the living Bread", Type.UNDEAD, 50, 90, 0, true, Status.CURSED, 30);
        Move siphon = new Move("Siphon", Type.UNDEAD, 35, 90, 0, true, Status.SIPHON, 100);
        Move foodPoisoning = new Move("Food Poisoning", Type.UNDEAD, 40, 90, 0, true, Status.POISON, 80);
        Move grandBanquet = new Move("Grand Banquet", Type.UNDEAD, 85, 90, 0, true, Status.CURSED, 50,
                Growth.ULT_COOLDOWN_TURNS);

        return new CharacterTemplate("Chefromancer", 85, 11, 10, 30, 33, 17,
                Type.UNDEAD, MoveKit.ladder(bakedbread, siphon, foodPoisoning, grandBanquet),
                List.of(ChefChickenPassive::new, ChefSlowCookPassive::new),
                List.of(StatKind.MAGIC_ATTACK, StatKind.MAGIC_DEFENSE));
    }

    public static CharacterTemplate healer() {
        Move heal = new Move("Heal", Type.HOLY, 0, 100, 0, true, Status.HEAL, 100);
        Move holySpark = new Move("Holy Spark", Type.HOLY, 35, 100, 0, true, Status.NONE, 0);
        Move barrier = new Move("Barrier", Type.HOLY, 80, 100, 0, true, Status.SHIELD, 100);
        Move holyNova = new Move("Holy Nova", Type.HOLY, 80, 100, 0, true, Status.NONE, 0,
                Growth.ULT_COOLDOWN_TURNS);

        return new CharacterTemplate("Okirik", 100, 5, 14, 30, 20, 16,
                Type.HOLY, MoveKit.ladder(heal, holySpark, barrier, holyNova),
                List.of(WomanBlessingPassive::new, WomanSparkMercyPassive::new),
                List.of(StatKind.MAGIC_ATTACK, StatKind.HP));
    }

    public static CharacterTemplate wizard() {
        Move bolt = new Move("Bolt", Type.LIGHTNING, 45, 95, 0, true, Status.PARALYSIS, 30);
        Move staticShock = new Move("Static", Type.LIGHTNING, 20, 90, 0, true, Status.PARALYSIS, 70);
        Move chain = new Move(WizardStormPassive.CHAIN_NAME, Type.LIGHTNING, 35, 90, 0, true, Status.PARALYSIS, 40);
        Move wrath = new Move(WizardStormPassive.WRATH_NAME, Type.LIGHTNING, 75, 90, 0, true, Status.PARALYSIS, 50,
                Growth.ULT_COOLDOWN_TURNS, 1, 1, true);

        return new CharacterTemplate("WEWE Head", 96, 8, 18, 30, 20, 20,
                Type.LIGHTNING, MoveKit.ladder(bolt, staticShock, chain, wrath),
                List.of(WizardHuntPassive::new, WizardChargedPassive::new, WizardStormPassive::new),
                List.of(StatKind.MAGIC_ATTACK, StatKind.SPEED));
    }

    public static CharacterTemplate sion() {
        Move slam = new Move("Decamating Slam", Type.PHYSICAL, 45, 60, 0, false, Status.STUN, 100);
        Move roar = SionExplodingShieldPassive.createMove();
        Move slow = new Move("Slow", Type.PHYSICAL, 20, 90, 0, false, Status.SLOW, 100);
        Move onslaught = new Move("Unstoppable Onslaught", Type.PHYSICAL, 100, 80, 0, false, Status.STUN, 100,
                Growth.ULT_COOLDOWN_TURNS);

        return new CharacterTemplate("Sion", 117, 13, 18, 10, 18, 8,
                Type.LIGHTNING, MoveKit.ladder(slam, roar, slow, onslaught),
                List.of(SionRevivePassive::new, SionExplodingShieldPassive::new,
                        SionKillMaxHpPassive::new, SionSlamPassive::new),
                List.of(StatKind.HP, StatKind.DEFENSE));
    }

    public static CharacterTemplate dualSwordsman() {
        Move tbolt = new Move("Thunderbolt", Type.LIGHTNING, 30, 90, 0, true, Status.SLOW, 15);
        Move dslash = new Move("Diagonal Strike", Type.PHYSICAL, 25, 90, 0, false, Status.BLEED, 15);
        Move lslash = new Move("Lightning slash", Type.LIGHTNING, 25, 90, 0, false, Status.PARALYSIS, 15);
        Move whirlwind = ElectricWhirlwindPassive.createMove();

        return new CharacterTemplate("Dual Swordsman", 75, 25, 25, 30, 15, 15,
                Type.PHYSICAL, MoveKit.ladder(tbolt, dslash, lslash, whirlwind),
                List.of(DualSwordsmanDoubleHitPassive::new, DualSwordsmanStatusRerollPassive::new,
                        ElectricWhirlwindPassive::new),
                List.of(StatKind.ATTACK, StatKind.MAGIC_ATTACK));
    }
    
    public static List<CharacterTemplate> all() {
        return List.of(knight(), rogue(), monk(), caveman(), chefromancer(), healer(), wizard(), sion(), dualSwordsman());
    }

    /**
     * Phase 2 combat stats on the current 4-move kits. Used by BalanceSim
     * 1v1 / 3v3 so those matches stay comparable to the old roster.
     */
    public static List<CharacterTemplate> arenaRoster() {
        return List.of(
                knight().withStats(200, 57, 37, 0, -20, 26),
                rogue().withStats(200, 60, 20, 20, 30, 70),
                monk().withStats(200, 45, 35, 40, 50, 30),
                caveman().withStats(220, 50, 70, 0, 40, 20),
                chefromancer().withStats(200, 21, 20, 60, 65, 34),
                healer().withStats(230, 10, 28, 60, 40, 32),
                wizard().withStats(210, 15, 35, 60, 40, 40),
                sion().withStats(270, 25, 35, 20, 35, 15),
                dualSwordsman().withStats(180, 50, 50, 30, 30, 60));
    }
}