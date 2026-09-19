package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;
import java.util.List;

public class CavemanFrenzyPassive implements Passive {

    public static final int ATTACK_INCREASE = 10;
    public static final int DEFENSE_INCREASE = 20;
    public static final int MAGIC_DEFENSE_INCREASE = 20;
    public static final int SPEED_INCREASE = 5;

    public static final int MAX_BUFFS = 3;
    private int buffCount = 0;

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {

        if (buffCount < MAX_BUFFS) {
            self.getStats().increaseAttack(ATTACK_INCREASE);
            self.getStats().increaseDefense(DEFENSE_INCREASE);
            self.getStats().increaseMagicDefense(MAGIC_DEFENSE_INCREASE);
            self.getStats().increaseSpeed(SPEED_INCREASE);
            log.add(self.getName() + " flies into a frenzy, he's growing stronger!");
            buffCount++;
        }
        
    }
}