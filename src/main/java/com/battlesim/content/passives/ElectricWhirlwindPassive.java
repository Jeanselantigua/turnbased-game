package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

/**
 * Dual Swordsman ult: a lightning slash (paralysis) followed by a physical
 * slash (bleed) so both statuses can land in one action.
 */
public class ElectricWhirlwindPassive implements Passive {

    public static final String MOVE_NAME = "Electric Whirlwind";
    public static final int HIT_POWER = 40;

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.LIGHTNING, HIT_POWER, 100, 0, false,
                Status.PARALYSIS, 100, 4);
    }

    public static Move physicalSlash() {
        return new Move(MOVE_NAME, Type.PHYSICAL, HIT_POWER, 100, 0, false,
                Status.BLEED, 100);
    }

    @Override
    public List<Move> followUpHits(Character self, Move move, List<Character> targets,
                                    BattleContext context, List<String> log) {
        if (move == null || !MOVE_NAME.equals(move.getName()) || move.getType() != Type.LIGHTNING) {
            return List.of();
        }
        log.add(self.getName() + "'s Electric Whirlwind also rips with steel!");
        return List.of(physicalSlash());
    }
}
