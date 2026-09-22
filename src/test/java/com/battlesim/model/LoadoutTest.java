package com.battlesim.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.item.GearFactory;
import com.battlesim.util.RandomProvider;
import org.junit.Test;

public class LoadoutTest {

    @Test
    public void equippingAppliesMainAndSubstatsAndUnequipReversesThem() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int hp = rogue.getStats().getMaxHp();
        int atk = rogue.getStats().getAttack();
        Inventory bag = new Inventory();
        Gear gloves = GearFactory.create(GearSlot.GLOVES, GearSet.WARLORD, GearRarity.LEGENDARY,
                new RandomProvider(7L));
        bag.add(gloves);

        assertTrue(rogue.equip(gloves, bag));
        assertTrue(bag.isEmpty());
        assertEquals(gloves, rogue.getLoadout().get(GearSlot.GLOVES));
        assertEquals(hp + gloves.bonus(StatKind.HP), rogue.getStats().getMaxHp());
        assertEquals(atk + gloves.bonus(StatKind.ATTACK), rogue.getStats().getAttack());

        Gear removed = rogue.unequip(GearSlot.GLOVES, bag);
        assertEquals(gloves, removed);
        assertEquals(1, bag.size());
        assertNull(rogue.getLoadout().get(GearSlot.GLOVES));
        assertEquals(hp, rogue.getStats().getMaxHp());
        assertEquals(atk, rogue.getStats().getAttack());
    }

    @Test
    public void swappingASlotBagsTheOldPiece() {
        Character knight = PlayableCharacters.knight().createInstance();
        Inventory bag = new Inventory();
        Gear first = GearFactory.create(GearSlot.HELM, GearSet.BULWARK, GearRarity.COMMON,
                new RandomProvider(1L));
        Gear second = GearFactory.create(GearSlot.HELM, GearSet.SAGE, GearRarity.RARE,
                new RandomProvider(2L));
        bag.add(first);
        bag.add(second);
        knight.equip(first, bag);
        knight.equip(second, bag);
        assertEquals(second, knight.getLoadout().get(GearSlot.HELM));
        assertTrue(bag.contains(first));
        assertFalse(bag.contains(second));
    }

    @Test
    public void upgradingEquippedGearAppliesTheDelta() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        Inventory bag = new Inventory();
        bag.addGold(10_000);
        Gear helm = GearFactory.create(GearSlot.HELM, GearSet.BULWARK, GearRarity.COMMON,
                new RandomProvider(1L));
        bag.add(helm);
        rogue.equip(helm, bag);
        int hpBefore = rogue.getStats().getMaxHp();
        assertTrue(rogue.upgradeGear(helm, bag, new RandomProvider(3L)));
        assertEquals(1, helm.getLevel());
        assertEquals(hpBefore + helm.getMainValue() - Gear.mainStatAt(StatKind.HP, 0),
                rogue.getStats().getMaxHp());
        assertEquals(10_000 - Gear.upgradeCost(0), bag.getGold());
    }
}
