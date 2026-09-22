package com.battlesim.progress;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Progression;
import com.battlesim.model.StatKind;
import java.util.List;

/**
 * Spreads leftover points across every stat, with one extra specialty slot
 * per cycle (~50% more points in prio stats). Specialty per-point bonuses
 * still apply. No-specialty kits (tests, enemies) stay strictly even.
 * The cycle continues from points already spent so 1-point level-ups
 * still even-spread instead of dumping into HP every time.
 */
public final class EvenStatAllocator implements StatAllocator {

    @Override
    public void allocate(Character character) {
        if (character == null) {
            return;
        }
        StatKind[] all = StatKind.pointStats();
        List<StatKind> specialties = character.getSpecialties();
        int cycleLength = all.length + (specialties.isEmpty() ? 0 : 1);
        int i = pointsAlreadySpent(character);
        int specIndex = extraSpecialtySlotsUsed(i, all.length, specialties.size());
        while (character.getUnspentStatPoints() > 0) {
            int slot = i % cycleLength;
            StatKind kind;
            if (slot < all.length) {
                kind = all[slot];
            } else {
                kind = specialties.get(specIndex % specialties.size());
                specIndex++;
            }
            character.spendStatPoint(kind);
            i++;
        }
        spendMovePoints(character);
    }

    private static void spendMovePoints(Character character) {
        while (character.getUnspentMovePoints() > 0) {
            Move target = pickMove(character);
            if (target == null || character.spendMovePoint(target) < 0) {
                return;
            }
        }
    }

    private static Move pickMove(Character character) {
        Move ult = character.getKit() != null ? character.getKit().getUlt() : null;
        if (ult != null && character.knowsMove(ult.getName())) {
            return ult;
        }
        List<Move> known = character.getMoves();
        if (known.isEmpty()) {
            return null;
        }
        return known.get(known.size() - 1);
    }

    private static int pointsAlreadySpent(Character character) {
        int granted = (character.getLevel() - 1) * Progression.STAT_POINTS_PER_LEVEL;
        return Math.max(0, granted - character.getUnspentStatPoints());
    }

    private static int extraSpecialtySlotsUsed(int spent, int statCount, int specialtyCount) {
        if (specialtyCount <= 0) {
            return 0;
        }
        return spent / (statCount + 1);
    }
}
