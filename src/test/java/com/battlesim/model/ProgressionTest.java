package com.battlesim.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProgressionTest {

    @Test
    public void xpToNextLevelIsOneHundredTimesCurrentLevel() {
        Progression progression = new Progression();
        assertEquals(1, progression.getLevel());
        assertEquals(100, progression.xpToNextLevel());
        progression.addXp(100);
        assertEquals(2, progression.getLevel());
        assertEquals(200, progression.xpToNextLevel());
    }

    @Test
    public void leftoverXpCarriesToTheNextLevel() {
        Progression progression = new Progression();
        assertEquals(1, progression.addXp(150));
        assertEquals(2, progression.getLevel());
        assertEquals(50, progression.getXp());
        assertEquals(Progression.STAT_POINTS_PER_LEVEL, progression.getUnspentStatPoints());
    }

    @Test
    public void multipleLevelsCanHappenFromOneGrant() {
        Progression progression = new Progression();
        // 100 + 200 + 300 = 600 reaches exactly level 4
        assertEquals(3, progression.addXp(600));
        assertEquals(4, progression.getLevel());
        assertEquals(0, progression.getXp());
        assertEquals(3 * Progression.STAT_POINTS_PER_LEVEL, progression.getUnspentStatPoints());
    }

    @Test
    public void movePointsStartAfterUltUnlockLevel() {
        Progression progression = new Progression();
        int xpTo19 = 50 * 19 * 18;
        progression.addXp(xpTo19);
        assertEquals(19, progression.getLevel());
        assertEquals(1, progression.getUnspentMovePoints());
        progression.addXp(1900);
        assertEquals(20, progression.getLevel());
        assertEquals(2, progression.getUnspentMovePoints());
    }

    @Test
    public void xpStopsAtTheLevelCap() {
        Progression progression = new Progression();
        progression.addXp(1_000_000);
        assertEquals(Progression.MAX_LEVEL, progression.getLevel());
        assertEquals(0, progression.getXp());
        assertEquals(0, progression.xpToNextLevel());
        assertEquals(Progression.STAT_POINTS_AT_CAP, progression.getUnspentStatPoints());
        assertEquals(0, progression.addXp(500));
    }

    @Test
    public void xpToReachLevelSumsTheCurve() {
        assertEquals(0, Progression.xpToReachLevel(1));
        assertEquals(100, Progression.xpToReachLevel(2));
        assertEquals(100 + 200 + 300, Progression.xpToReachLevel(4));
        assertEquals(100 * 29 * 30 / 2, Progression.xpToReachLevel(Progression.MAX_LEVEL));
    }
}
