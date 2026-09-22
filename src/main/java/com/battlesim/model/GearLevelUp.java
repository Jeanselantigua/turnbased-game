package com.battlesim.model;

/** What changed when a piece gained one enhancement level. */
public final class GearLevelUp {

    private final int previousLevel;
    private final int newLevel;
    private final StatKind mainKind;
    private final int mainDelta;
    private final StatKind substatKind;
    private final int subDelta;
    private final boolean newSubstat;

    public GearLevelUp(int previousLevel, int newLevel, StatKind mainKind, int mainDelta,
                       StatKind substatKind, int subDelta, boolean newSubstat) {
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.mainKind = mainKind;
        this.mainDelta = mainDelta;
        this.substatKind = substatKind;
        this.subDelta = subDelta;
        this.newSubstat = newSubstat;
    }

    public static GearLevelUp none(Gear gear) {
        int level = gear == null ? 0 : gear.getLevel();
        StatKind main = gear == null ? null : gear.getMainKind();
        return new GearLevelUp(level, level, main, 0, null, 0, false);
    }

    public boolean leveled() {
        return newLevel > previousLevel;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public StatKind getMainKind() {
        return mainKind;
    }

    public int getMainDelta() {
        return mainDelta;
    }

    public StatKind getSubstatKind() {
        return substatKind;
    }

    public int getSubDelta() {
        return subDelta;
    }

    public boolean isNewSubstat() {
        return newSubstat;
    }
}
