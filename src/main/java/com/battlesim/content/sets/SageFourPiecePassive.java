package com.battlesim.content.sets;

import com.battlesim.item.SetBonusPassive;
import com.battlesim.item.SetBonuses;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.StatKind;
import java.util.ArrayList;
import java.util.List;

/**
 * Sage 4-piece: while this wearer is below 75% HP, living allies gain +10 Speed
 * (+20 Speed below 50% HP).
 */
public final class SageFourPiecePassive implements SetBonusPassive {

    private final List<Character> buffed = new ArrayList<>();
    private int appliedSpeed;

    public int getAppliedSpeed() {
        return appliedSpeed;
    }

    @Override
    public void onTurnStart(Character self, BattleContext context, List<String> log) {
        refresh(self, context, log);
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        refresh(self, context, log);
    }

    @Override
    public void onDamageTaken(Character self, Character attacker, int damageTaken,
                              List<String> log, BattleContext context) {
        refresh(self, context, log);
    }

    @Override
    public void onHealed(Character self, int amount, List<String> log) {
        retune(self);
    }

    @Override
    public void onFaint(Character self, Character killer, com.battlesim.model.Move move,
                        BattleContext context, List<String> log) {
        clearBuffs();
    }

    @Override
    public void clearBonus(Character wearer) {
        clearBuffs();
    }

    private void refresh(Character self, BattleContext context, List<String> log) {
        if (self == null || context == null || self.isFainted()) {
            clearBuffs();
            return;
        }
        int wanted = SetBonuses.sageAllySpeed(self);
        clearBuffs();
        if (wanted <= 0) {
            return;
        }
        for (Character ally : context.alliesOf(self)) {
            if (ally == self || ally.isFainted() || ally.isSummon()) {
                continue;
            }
            ally.getStats().add(StatKind.SPEED, wanted);
            buffed.add(ally);
        }
        appliedSpeed = wanted;
        if (log != null && !buffed.isEmpty()) {
            log.add(self.getName() + "'s Sage set quickens allies (+" + wanted + " Speed)!");
        }
    }

    private void retune(Character self) {
        int wanted = self == null || self.isFainted() ? 0 : SetBonuses.sageAllySpeed(self);
        if (wanted == 0) {
            clearBuffs();
            return;
        }
        if (wanted == appliedSpeed) {
            return;
        }
        int delta = wanted - appliedSpeed;
        for (Character ally : buffed) {
            ally.getStats().add(StatKind.SPEED, delta);
        }
        appliedSpeed = wanted;
    }

    private void clearBuffs() {
        for (Character ally : buffed) {
            ally.getStats().add(StatKind.SPEED, -appliedSpeed);
        }
        buffed.clear();
        appliedSpeed = 0;
    }
}
