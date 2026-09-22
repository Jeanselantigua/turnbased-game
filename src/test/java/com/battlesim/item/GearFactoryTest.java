package com.battlesim.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Gear;
import com.battlesim.model.GearAffix;
import com.battlesim.model.GearRarity;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.StatKind;
import com.battlesim.util.RandomProvider;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class GearFactoryTest {

    @Test
    public void helmMainStatIsAlwaysHp() {
        Gear helm = GearFactory.create(GearSlot.HELM, GearSet.WARLORD, GearRarity.COMMON,
                new RandomProvider(1L));
        assertEquals(StatKind.HP, helm.getMainKind());
        assertEquals(Gear.mainStatAt(StatKind.HP, 0), helm.getMainValue());
        assertEquals(0, helm.getSubstats().size());
    }

    @Test
    public void legendaryStartsWithThreeUniqueSubstatsNotMatchingMain() {
        Gear gloves = GearFactory.create(GearSlot.GLOVES, GearSet.SWIFT, GearRarity.LEGENDARY,
                new RandomProvider(9L));
        assertEquals(StatKind.ATTACK, gloves.getMainKind());
        assertEquals(3, gloves.getSubstats().size());
        Set<StatKind> kinds = new HashSet<>();
        for (GearAffix affix : gloves.getSubstats()) {
            assertTrue(affix.getValue() > 0);
            assertFalse(affix.getKind() == StatKind.ATTACK);
            assertTrue(kinds.add(affix.getKind()));
        }
    }

    @Test
    public void ringMainStatComesFromItsPoolAndIsNotASubstat() {
        Gear ring = GearFactory.create(GearSlot.RING, GearSet.SAGE, GearRarity.RARE,
                new RandomProvider(4L));
        assertTrue(GearSlot.RING.getMainStatPool().contains(ring.getMainKind()));
        assertEquals(1, ring.getSubstats().size());
        assertTrue(ring.getSubstats().get(0).getKind() != ring.getMainKind());
        assertEquals(ring.getMainValue(), ring.bonus(ring.getMainKind()));
    }
}
