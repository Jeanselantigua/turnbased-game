package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;
import java.util.List;

public class CavemanFrenzyPassive implements Passive {

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        self.getStats().increaseAttack(8);
        self.getStats().increaseDefense(10);
        self.getStats().increaseMagicDefense(10);
        log.add(self.getName() + " flies into a frenzy, he's growing stronger!");
    }
}