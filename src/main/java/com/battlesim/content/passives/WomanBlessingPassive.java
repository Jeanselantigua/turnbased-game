package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import java.util.ArrayList;
import java.util.List;

/** After a Heal, the target is cleansed and their next incoming hit is reduced. */
public class WomanBlessingPassive implements Passive {

    private static final double NEXT_HIT_REDUCTION = 0.40;
    private Character blessed;

    @Override
    public void onAllyHealed(Character self, Character target, int amount, List<String> log) {
        cleanseHarmfulStatuses(target, log);
        if (amount <= 0) {
            return;
        }
        blessed = target;
        log.add(target.getName() + " is blessed and will take less damage from the next hit!");
    }

    private static void cleanseHarmfulStatuses(Character target, List<String> log) {
        List<Status> debuffs = new ArrayList<>();
        for (Status status : target.getActiveStatuses()) {
            if (status.isDebuff()) {
                debuffs.add(status);
            }
        }
        boolean hadSiphon = target.isSiphoned();
        boolean hadWounds = target.getWoundStacks() > 0;
        if (debuffs.isEmpty() && !hadSiphon && !hadWounds) {
            return;
        }
        for (Status status : debuffs) {
            target.clearStatus(status);
        }
        if (hadSiphon) {
            target.clearSiphon();
        }
        if (hadWounds) {
            target.consumeWoundStacks();
        }
        log.add(target.getName() + " is cleansed of harmful statuses!");
    }

    @Override
    public double modifyIncomingDamageToAlly(Character self, Character ally, Character attacker,
                                              Move move, double damage, List<String> log) {
        if (blessed == null || ally != blessed || damage <= 0) {
            return damage;
        }
        blessed = null;
        log.add(ally.getName() + "'s blessing softens the blow!");
        return damage * (1.0 - NEXT_HIT_REDUCTION);
    }
}
