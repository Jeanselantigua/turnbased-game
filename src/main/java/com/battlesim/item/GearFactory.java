package com.battlesim.item;

import com.battlesim.model.Gear;
import com.battlesim.model.GearAffix;
import com.battlesim.model.GearRarity;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.StatKind;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/** Rolls a fresh +0 piece: slot-locked main, rarity-based starting substats. */
public final class GearFactory {

    private GearFactory() {
    }

    public static Gear random(GearRarity rarity, RandomProvider random) {
        GearSlot slot = pick(GearSlot.values(), random);
        GearSet set = pick(GearSet.values(), random);
        return create(slot, set, rarity, random);
    }

    public static Gear create(GearSlot slot, GearSet set, GearRarity rarity, RandomProvider random) {
        if (slot == null || set == null || rarity == null || random == null) {
            throw new IllegalArgumentException("Need slot, set, rarity, and randomness");
        }
        List<StatKind> mains = slot.getMainStatPool();
        StatKind mainKind = mains.get(random.nextInt(0, mains.size() - 1));
        int mainValue = Gear.mainStatAt(mainKind, 0);
        List<GearAffix> subs = new ArrayList<>();
        int starting = Math.min(rarity.getStartingSubstats(), Gear.MAX_SUBSTATS);
        List<StatKind> pool = substatPool(mainKind, subs);
        for (int i = 0; i < starting && !pool.isEmpty(); i++) {
            StatKind kind = pool.get(random.nextInt(0, pool.size() - 1));
            pool.remove(kind);
            subs.add(new GearAffix(kind, Gear.rollSubValue(kind, random)));
        }
        return new Gear(slot, set, rarity, mainKind, 0, mainValue, subs);
    }

    private static List<StatKind> substatPool(StatKind mainKind, List<GearAffix> already) {
        List<StatKind> pool = new ArrayList<>();
        for (StatKind kind : StatKind.values()) {
            if (kind == mainKind) {
                continue;
            }
            boolean taken = false;
            for (GearAffix affix : already) {
                if (affix.getKind() == kind) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                pool.add(kind);
            }
        }
        return pool;
    }

    private static <T> T pick(T[] values, RandomProvider random) {
        return values[random.nextInt(0, values.length - 1)];
    }
}
