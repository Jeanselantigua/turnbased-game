package com.battlesim.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Gear;
import com.battlesim.model.GearLevelUp;
import com.battlesim.model.GearRarity;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.StatKind;
import com.battlesim.util.RandomProvider;
import org.junit.Test;

public class GearUpgradeTest {

    @Test
    public void mainStatGrowsEveryLevel() {
        Gear helm = GearFactory.create(GearSlot.HELM, GearSet.BULWARK, GearRarity.COMMON,
                new RandomProvider(1L));
        int start = helm.getMainValue();
        GearLevelUp first = helm.levelUp(new RandomProvider(2L));
        assertEquals(1, helm.getLevel());
        assertEquals(Gear.mainStatAt(StatKind.HP, 1), helm.getMainValue());
        assertEquals(helm.getMainValue() - start, first.getMainDelta());
        assertEquals(0, helm.getSubstats().size());
    }

    @Test
    public void substatsRollOnEveryThirdLevelAndCapAtFive() {
        Gear helm = GearFactory.create(GearSlot.HELM, GearSet.WARLORD, GearRarity.COMMON,
                new RandomProvider(1L));
        RandomProvider random = new RandomProvider(11L);
        int subEvents = 0;
        for (int i = 0; i < Gear.MAX_LEVEL; i++) {
            GearLevelUp result = helm.levelUp(random);
            if (result.getSubDelta() > 0) {
                subEvents++;
            }
        }
        assertEquals(Gear.MAX_LEVEL, helm.getLevel());
        assertEquals(Gear.MAX_LEVEL / Gear.SUBSTAT_EVERY, subEvents);
        assertEquals(Gear.MAX_SUBSTATS, helm.getSubstats().size());
        assertEquals(Gear.mainStatAt(StatKind.HP, Gear.MAX_LEVEL), helm.getMainValue());
    }

    @Test
    public void legendaryFillsThenUpgradesExistingSubstats() {
        Gear gloves = GearFactory.create(GearSlot.GLOVES, GearSet.SWIFT, GearRarity.LEGENDARY,
                new RandomProvider(3L));
        assertEquals(3, gloves.getSubstats().size());
        int startTotal = substatTotal(gloves);
        RandomProvider random = new RandomProvider(21L);
        int newLines = 0;
        int upgrades = 0;
        for (int i = 0; i < Gear.MAX_LEVEL; i++) {
            GearLevelUp result = gloves.levelUp(random);
            if (result.isNewSubstat()) {
                newLines++;
            } else if (result.getSubDelta() > 0) {
                upgrades++;
            }
        }
        assertEquals(Gear.MAX_SUBSTATS - 3, newLines);
        assertEquals(Gear.MAX_LEVEL / Gear.SUBSTAT_EVERY - (Gear.MAX_SUBSTATS - 3), upgrades);
        assertEquals(Gear.MAX_SUBSTATS, gloves.getSubstats().size());
        assertTrue(substatTotal(gloves) > startTotal);
    }

    @Test
    public void cannotLevelPastTheCap() {
        Gear boots = GearFactory.create(GearSlot.BOOTS, GearSet.SWIFT, GearRarity.COMMON,
                new RandomProvider(1L));
        RandomProvider random = new RandomProvider(5L);
        for (int i = 0; i < Gear.MAX_LEVEL; i++) {
            boots.levelUp(random);
        }
        assertEquals(Gear.MAX_LEVEL, boots.getLevel());
        GearLevelUp extra = boots.levelUp(random);
        assertEquals(Gear.MAX_LEVEL, extra.getNewLevel());
        assertEquals(0, extra.getMainDelta());
        assertEquals(0, Gear.upgradeCost(Gear.MAX_LEVEL));
        assertEquals(Gear.goldToReach(1), Gear.upgradeCost(0));
    }

    @Test
    public void thirtyLevelsCostTheSameGoldAsTheOldFifteenCurve() {
        int spent = 0;
        for (int level = 0; level < Gear.MAX_LEVEL; level++) {
            spent += Gear.upgradeCost(level);
        }
        assertEquals(3000, Gear.TOTAL_UPGRADE_GOLD);
        assertEquals(Gear.TOTAL_UPGRADE_GOLD, spent);
        assertEquals(Gear.TOTAL_UPGRADE_GOLD, Gear.goldToReach(Gear.MAX_LEVEL));
        assertTrue(Gear.upgradeCost(0) < Gear.upgradeCost(Gear.MAX_LEVEL - 1));
    }

    private static int substatTotal(Gear gear) {
        int total = 0;
        for (int i = 0; i < gear.getSubstats().size(); i++) {
            total += gear.getSubstats().get(i).getValue();
        }
        return total;
    }
}
