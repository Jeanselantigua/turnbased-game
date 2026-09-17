package com.battlesim.content;

import com.battlesim.content.passives.CavemanAccuracyCreepPassive;
import com.battlesim.content.passives.CavemanFrenzyPassive;
import com.battlesim.content.passives.KnightBleedPassive;
import com.battlesim.content.passives.KnightShieldPassive;
import com.battlesim.content.passives.MonkHolySplitPassive;
import com.battlesim.content.passives.MonkParryPassive;
import com.battlesim.content.passives.RogueCritStealthPassive;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

public class PlayableCharacters {

    public static CharacterTemplate knight() {
        Move slash = new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0);
        
        
        return new CharacterTemplate("Knight", 240, 45, 40, 10, 15, 30,
                Type.PHYSICAL, List.of(slash),
                List.of(KnightBleedPassive::new, KnightShieldPassive::new));
    }

    public static CharacterTemplate rogue() {
        Move backstab = new Move("Assasinate", Type.PHYSICAL, 60, 80, 1, false, Status.NONE, 0);
        Move crossbow = new Move("Crossbow", Type.PHYSICAL, 90, 90, 0, false, Status.NONE, 0);
        
        
        return new CharacterTemplate("Rogue", 120, 45, 30, 20, 10, 60,
                Type.ARCANE, List.of(backstab, crossbow),
                List.of(RogueCritStealthPassive::new));
    }

    public static CharacterTemplate monk() {
        Move oochie = new Move("Oochie", Type.HOLY, 70, 100, 0, false, Status.NONE, 0);
        
        
        return new CharacterTemplate("Roeseph", 180, 40, 40, 40, 40, 30,
                Type.HOLY, List.of(oochie),
                List.of(MonkHolySplitPassive::new, MonkParryPassive::new));
    }

    public static CharacterTemplate caveman() {
        Move club = new Move("Bonk", Type.PHYSICAL, 120, 30, 0, false, Status.STUN, 40);
        
        return new CharacterTemplate("Randy", 200, 70, 60, 0, 35, 25,
                Type.PHYSICAL, List.of(club),
                List.of(CavemanFrenzyPassive::new, CavemanAccuracyCreepPassive::new));
    }
}