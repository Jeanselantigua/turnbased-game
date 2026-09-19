package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import java.util.List;

public class SionRevivePassive implements Passive {

    public static final int MAX_REVIVES = 1;
    public static final String UNDEAD_NAME = "Undead Sion";

    private int currentRevives = 0;

    @Override
    public void onFaint(Character self, Character killer, Move move, BattleContext context, List<String> log) {
        if (context == null || !self.isFainted() || currentRevives >= MAX_REVIVES) {
            return;
        }
        context.addAlly(self, revivedSion(), log);
        currentRevives++;
    }

    private static Character revivedSion() {
        Move punch = new Move("Punch", Type.UNDEAD, 25, 100, 0, false, Status.NONE, 0);
        Stats stats = new Stats(200, 30, 30, 0, 30, 45);
        return new Character(UNDEAD_NAME, stats, Type.UNDEAD, List.of(punch));
    }
}
