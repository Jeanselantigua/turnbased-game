package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import java.util.List;
import java.util.Random;

public class DualSwordsmanDoubleHitPassive implements Passive {

    static final double REPEAT_CHANCE = 0.25;
    private final Random random;

    public DualSwordsmanDoubleHitPassive() {
        this(new Random());
    }

    DualSwordsmanDoubleHitPassive(Random random) {
        this.random = random;
    }

    @Override
    public boolean shouldRepeatAction(Character self, Move move, List<Character> targets,
                                       BattleContext context, List<String> log) {
        if (move.targetsAllies() || ElectricWhirlwindPassive.MOVE_NAME.equals(move.getName())
                || random.nextDouble() >= REPEAT_CHANCE) {
            return false;
        }
        log.add(self.getName() + " strikes again with " + move.getName() + "!");
        return true;
    }
}
