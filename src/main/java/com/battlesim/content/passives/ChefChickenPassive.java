package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;
import java.util.Random;

public class ChefChickenPassive implements Passive {

    private static final double SUMMON_CHANCE = 0.30;
    private final Random random = new Random();
    private Character chicken;

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log,
                             BattleContext context) {
        if (context == null || move.getType() != Type.UNDEAD || damageDealt <= 0) {
            return;
        }
        if (chicken != null && !chicken.isFainted()) {
            return;
        }
        if (random.nextDouble() >= SUMMON_CHANCE) {
            return;
        }
        chicken = createChicken();
        context.summonAlly(self, chicken, log);
    }

    private static Character createChicken() {
        Move peck = new Move("Peck", Type.UNDEAD, 25, 100, 0, false, Status.NONE, 0);
        Stats stats = new Stats(40, 15, 8, 5, 8, 38);
        return new Character("Chicken Spirit", stats, Type.UNDEAD, List.of(peck));
    }
}
