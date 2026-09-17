package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;

public class CavemanAccuracyCreepPassive implements Passive {

    private int bonusAccuracy = 0;

    @Override
    public int modifyAccuracy(Character self, Move move, int baseAccuracy) {
        return baseAccuracy + bonusAccuracy;
    }

    @Override
    public void onAttackMissed(Character self, Move move, List<String> log) {
        bonusAccuracy += 5;
        log.add(self.getName() + " grows more determined! (+5 accuracy)");
    }

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log) {
        bonusAccuracy = 0;
    }
}