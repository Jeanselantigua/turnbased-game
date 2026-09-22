package com.battlesim.model;

import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One equippable piece: a slot-locked main stat that grows every level, plus
 * up to {@link #MAX_SUBSTATS} substats that roll every {@link #SUBSTAT_EVERY} levels.
 */
public final class Gear {

    public static final int MAX_LEVEL = 30;
    public static final int MAX_SUBSTATS = 5;
    public static final int SUBSTAT_EVERY = 3;
    /** Gold to take a piece from +0 to the cap — same total as the old +15 curve. */
    public static final int TOTAL_UPGRADE_GOLD = 25 * 15 * 16 / 2;

    private final GearSlot slot;
    private final GearSet set;
    private final GearRarity rarity;
    private final StatKind mainKind;
    private int level;
    private int mainValue;
    private final List<GearAffix> substats = new ArrayList<>();

    public Gear(GearSlot slot, GearSet set, GearRarity rarity, StatKind mainKind,
                int level, int mainValue, List<GearAffix> substats) {
        if (slot == null || set == null || rarity == null || mainKind == null) {
            throw new IllegalArgumentException("Gear needs slot, set, rarity, and a main stat");
        }
        if (!slot.getMainStatPool().contains(mainKind)) {
            throw new IllegalArgumentException(slot.getLabel() + " cannot roll " + mainKind.getLabel());
        }
        this.slot = slot;
        this.set = set;
        this.rarity = rarity;
        this.mainKind = mainKind;
        this.level = Math.max(0, Math.min(MAX_LEVEL, level));
        this.mainValue = Math.max(0, mainValue);
        if (substats != null) {
            for (GearAffix affix : substats) {
                if (affix != null) {
                    this.substats.add(affix);
                }
            }
        }
    }

    public GearSlot getSlot() {
        return slot;
    }

    public GearSet getSet() {
        return set;
    }

    public GearRarity getRarity() {
        return rarity;
    }

    public StatKind getMainKind() {
        return mainKind;
    }

    public int getLevel() {
        return level;
    }

    public int getMainValue() {
        return mainValue;
    }

    public List<GearAffix> getSubstats() {
        return Collections.unmodifiableList(substats);
    }

    public int bonus(StatKind kind) {
        if (kind == null) {
            return 0;
        }
        int total = kind == mainKind ? mainValue : 0;
        for (GearAffix affix : substats) {
            if (affix.getKind() == kind) {
                total += affix.getValue();
            }
        }
        return total;
    }

    public GearAffix substat(StatKind kind) {
        for (GearAffix affix : substats) {
            if (affix.getKind() == kind) {
                return affix;
            }
        }
        return null;
    }

    /**
     * Gold to go from {@code currentLevel} to the next. 0 at the cap.
     * Costs rise with level and the 30 upgrades still sum to {@link #TOTAL_UPGRADE_GOLD}.
     */
    public static int upgradeCost(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= MAX_LEVEL) {
            return 0;
        }
        return goldToReach(currentLevel + 1) - goldToReach(currentLevel);
    }

    /** Gold spent to take a piece from +0 to {@code level}. */
    public static int goldToReach(int level) {
        int reached = Math.max(0, Math.min(MAX_LEVEL, level));
        if (reached == 0) {
            return 0;
        }
        int triangleTo = reached * (reached + 1) / 2;
        int triangleMax = MAX_LEVEL * (MAX_LEVEL + 1) / 2;
        return TOTAL_UPGRADE_GOLD * triangleTo / triangleMax;
    }

    public static int mainStatAt(StatKind kind, int gearLevel) {
        int bounded = Math.max(0, Math.min(MAX_LEVEL, gearLevel));
        if (kind == StatKind.HP) {
            return 14 + 2 * bounded;
        }
        if (kind == StatKind.SPEED) {
            return 3 + bounded / 2;
        }
        if (kind != null && kind.isPercent()) {
            return 4 + bounded / 2;
        }
        return 6 + bounded;
    }

    public static int rollSubValue(StatKind kind, RandomProvider random) {
        if (kind == null || random == null) {
            return 0;
        }
        if (kind == StatKind.HP) {
            return random.nextInt(5, 8);
        }
        if (kind == StatKind.SPEED) {
            return random.nextInt(1, 2);
        }
        if (kind == StatKind.CRIT_RATE) {
            return random.nextInt(2, 4);
        }
        if (kind == StatKind.CRIT_DAMAGE) {
            return random.nextInt(4, 8);
        }
        return random.nextInt(2, 4);
    }

    /**
     * +1 level. Main stat retunes every time. Every {@link #SUBSTAT_EVERY}
     * levels, either unlock a new substat (until five) or upgrade a random one.
     */
    public GearLevelUp levelUp(RandomProvider random) {
        if (random == null || level >= MAX_LEVEL) {
            return GearLevelUp.none(this);
        }
        int previous = level;
        int oldMain = mainValue;
        level++;
        mainValue = mainStatAt(mainKind, level);
        int mainDelta = mainValue - oldMain;
        StatKind subKind = null;
        int subDelta = 0;
        boolean isNew = false;
        if (level % SUBSTAT_EVERY == 0) {
            SubRoll roll = rollSubstat(random);
            subKind = roll.kind;
            subDelta = roll.delta;
            isNew = roll.isNew;
        }
        return new GearLevelUp(previous, level, mainKind, mainDelta, subKind, subDelta, isNew);
    }

    private SubRoll rollSubstat(RandomProvider random) {
        if (substats.size() < MAX_SUBSTATS) {
            List<StatKind> pool = availableSubKinds();
            if (pool.isEmpty()) {
                return upgradeRandomSub(random);
            }
            StatKind kind = pool.get(random.nextInt(0, pool.size() - 1));
            int value = rollSubValue(kind, random);
            substats.add(new GearAffix(kind, value));
            return new SubRoll(kind, value, true);
        }
        return upgradeRandomSub(random);
    }

    private SubRoll upgradeRandomSub(RandomProvider random) {
        if (substats.isEmpty()) {
            return new SubRoll(null, 0, false);
        }
        GearAffix affix = substats.get(random.nextInt(0, substats.size() - 1));
        int value = rollSubValue(affix.getKind(), random);
        affix.add(value);
        return new SubRoll(affix.getKind(), value, false);
    }

    private List<StatKind> availableSubKinds() {
        List<StatKind> pool = new ArrayList<>();
        for (StatKind kind : StatKind.values()) {
            if (kind == mainKind || substat(kind) != null) {
                continue;
            }
            pool.add(kind);
        }
        return pool;
    }

    public String summary() {
        return rarity.getLabel() + " " + set.getLabel() + " " + slot.getLabel()
                + " +" + level + "  " + mainKind.getLabel() + " +" + mainValue;
    }

    public String describe() {
        StringBuilder text = new StringBuilder();
        text.append(summary());
        if (!substats.isEmpty()) {
            text.append("  [");
            for (int i = 0; i < substats.size(); i++) {
                if (i > 0) {
                    text.append(", ");
                }
                GearAffix affix = substats.get(i);
                text.append(affix.getKind().formatBonus(affix.getValue()));
            }
            text.append("]");
        }
        return text.toString();
    }

    private static final class SubRoll {
        final StatKind kind;
        final int delta;
        final boolean isNew;

        SubRoll(StatKind kind, int delta, boolean isNew) {
            this.kind = kind;
            this.delta = delta;
            this.isNew = isNew;
        }
    }
}
