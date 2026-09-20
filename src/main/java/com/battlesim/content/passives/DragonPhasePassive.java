package com.battlesim.content.passives;

import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.model.Character;
import java.util.List;

public class DragonPhasePassive implements Passive {
    public enum Phase { ONE, TWO, THREE }

    private Phase phase = Phase.ONE;

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken, List<String> log) {
        if (self.isFainted()) return;

        double hp = self.getStats().getCurrentHp() / (double) self.getStats().getMaxHp();

        if (phase == Phase.ONE && hp <= 0.50) {
            enterPhaseTwo(self, log);
        }
        if (phase == Phase.TWO && hp <= 0.25) {
            enterPhaseThree(self, log);
        }
    }

    private void enterPhaseTwo(Character self, List<String> log) {
        phase = Phase.TWO;
        self.addMove(new Move("Inferno", Type.FIRE, 80, 90, 0, true, Status.BURN, 100));
        self.addPassive(new ExtraActionsPassive(1));
        log.add(self.getName() + " enrages! It up to something.");
    }

    private void enterPhaseThree(Character self, List<String> log) {
        phase = Phase.THREE;
        self.addPassive(new ExtraActionsPassive(1));
        self.getStats().increaseSpeed(10);
        self.getStats().increaseAttack(10);
        self.getStats().increaseDefense(10);
        self.getStats().increaseMagicDefense(10);
        self.getStats().increaseMagicAttack(10);
        self.getStats().increaseMagicAttack(10);
        log.add(self.getName() + " screeches. It's started its last stand");
    }
}