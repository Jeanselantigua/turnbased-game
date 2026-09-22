package com.battlesim.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.content.passives.RogueCritStealthPassive;
import com.battlesim.item.SetBonuses;
import java.util.List;
import org.junit.Test;

public class CritStatsTest {

    @Test
    public void monkStartsWithNoCritWithoutGear() {
        Character monk = PlayableCharacters.monk().createInstance();
        assertEquals(0, monk.getCritRate());
        assertEquals(0, monk.getCritDamage());
        assertEquals(1.0, monk.critMultiplier(null), 0.0001);
    }

    @Test
    public void roguePassiveShowsOnTheSheet() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        assertEquals(15, rogue.getCritRate());
        assertEquals(RogueCritStealthPassive.CRIT_DAMAGE_PERCENT, rogue.getCritDamage());
        assertEquals(2.0, rogue.critMultiplier(null), 0.0001);
    }

    @Test
    public void critCannotReceiveStatPoints() {
        Character monk = PlayableCharacters.monk().createInstance();
        monk.grantXp(100);
        assertEquals(Progression.STAT_POINTS_PER_LEVEL, monk.getUnspentStatPoints());
        assertEquals(0, monk.spendStatPoint(StatKind.CRIT_RATE));
        assertEquals(0, monk.spendStatPoint(StatKind.CRIT_DAMAGE));
        assertEquals(Progression.STAT_POINTS_PER_LEVEL, monk.getUnspentStatPoints());
        assertEquals(0, monk.getCritRate());
        assertEquals(0, monk.getCritDamage());
    }

    @Test
    public void gearCritSubstatsApplyAndUnequip() {
        Character monk = PlayableCharacters.monk().createInstance();
        Inventory bag = new Inventory();
        Gear helm = new Gear(GearSlot.HELM, GearSet.SWIFT, GearRarity.COMMON, StatKind.HP,
                0, Gear.mainStatAt(StatKind.HP, 0),
                List.of(new GearAffix(StatKind.CRIT_RATE, 8), new GearAffix(StatKind.CRIT_DAMAGE, 12)));
        bag.add(helm);
        monk.equip(helm, bag);
        assertEquals(8, monk.getCritRate());
        assertEquals(12, monk.getCritDamage());
        monk.unequip(GearSlot.HELM, bag);
        assertEquals(0, monk.getCritRate());
        assertEquals(0, monk.getCritDamage());
    }

    @Test
    public void swiftFourPieceShowsCritDamageWithoutCritRate() {
        Character monk = PlayableCharacters.monk().createInstance();
        monk.getStats().add(StatKind.SPEED, 40);
        Inventory bag = new Inventory();
        for (GearSlot slot : new GearSlot[] {
                GearSlot.HELM, GearSlot.GLOVES, GearSlot.CHEST, GearSlot.BOOTS }) {
            Gear piece = new Gear(slot, GearSet.SWIFT, GearRarity.COMMON,
                    slot.getMainStatPool().get(0), 0,
                    Gear.mainStatAt(slot.getMainStatPool().get(0), 0), List.of());
            bag.add(piece);
            monk.equip(piece, bag);
        }
        assertTrue(monk.getStats().getSpeed() > SetBonuses.SWIFT_SPEED_TIER_1);
        assertTrue(monk.getStats().getSpeed() <= SetBonuses.SWIFT_SPEED_TIER_2);
        assertEquals(0, monk.getCritRate());
        assertEquals(20, monk.getCritDamage());
    }
}
