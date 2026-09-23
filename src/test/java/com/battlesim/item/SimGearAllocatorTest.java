package com.battlesim.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearRarity;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.Inventory;
import com.battlesim.model.StatKind;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class SimGearAllocatorTest {

    @Test
    public void preferredSetFollowsKitSpecialties() {
        assertEquals(GearSet.BULWARK, SimGearAllocator.preferredSet(
                PlayableCharacters.knight().createInstance()));
        assertEquals(GearSet.SWIFT, SimGearAllocator.preferredSet(
                PlayableCharacters.rogue().createInstance()));
        assertEquals(GearSet.SAGE, SimGearAllocator.preferredSet(
                PlayableCharacters.wizard().createInstance()));
        assertEquals(GearSet.WARLORD, SimGearAllocator.preferredSet(
                PlayableCharacters.caveman().createInstance()));
    }

    @Test
    public void outfitsFourPieceOfThePreferredSetOverHigherRarityOffSet() {
        Character knight = PlayableCharacters.knight().createInstance();
        Inventory bag = new Inventory();
        bag.add(piece(GearSlot.HELM, GearSet.BULWARK, GearRarity.COMMON, StatKind.HP));
        bag.add(piece(GearSlot.GLOVES, GearSet.BULWARK, GearRarity.COMMON, StatKind.ATTACK));
        bag.add(piece(GearSlot.CHEST, GearSet.BULWARK, GearRarity.COMMON, StatKind.DEFENSE));
        bag.add(piece(GearSlot.BOOTS, GearSet.BULWARK, GearRarity.COMMON, StatKind.SPEED));
        bag.add(piece(GearSlot.HELM, GearSet.WARLORD, GearRarity.LEGENDARY, StatKind.HP));
        bag.add(piece(GearSlot.GLOVES, GearSet.WARLORD, GearRarity.LEGENDARY, StatKind.ATTACK));
        bag.add(piece(GearSlot.CHEST, GearSet.WARLORD, GearRarity.LEGENDARY, StatKind.DEFENSE));
        bag.add(piece(GearSlot.BOOTS, GearSet.WARLORD, GearRarity.LEGENDARY, StatKind.SPEED));

        SimGearAllocator.outfit(List.of(knight), bag);

        assertEquals(GearSet.BULWARK, knight.getLoadout().get(GearSlot.HELM).getSet());
        assertEquals(GearSet.BULWARK, knight.getLoadout().get(GearSlot.GLOVES).getSet());
        assertEquals(GearSet.BULWARK, knight.getLoadout().get(GearSlot.CHEST).getSet());
        assertEquals(GearSet.BULWARK, knight.getLoadout().get(GearSlot.BOOTS).getSet());
        assertEquals(GearSet.FOUR_PIECE, SetBonuses.count(knight.getLoadout(), GearSet.BULWARK));
    }

    @Test
    public void secondTeammateDoesNotStripTheFirstCharactersFourPiece() {
        Character tank = PlayableCharacters.knight().createInstance();
        Character sion = PlayableCharacters.sion().createInstance();
        Inventory bag = new Inventory();
        bag.add(piece(GearSlot.HELM, GearSet.BULWARK, GearRarity.RARE, StatKind.HP));
        bag.add(piece(GearSlot.GLOVES, GearSet.BULWARK, GearRarity.RARE, StatKind.ATTACK));
        bag.add(piece(GearSlot.CHEST, GearSet.BULWARK, GearRarity.RARE, StatKind.DEFENSE));
        bag.add(piece(GearSlot.BOOTS, GearSet.BULWARK, GearRarity.RARE, StatKind.SPEED));
        bag.add(piece(GearSlot.AMULET, GearSet.SAGE, GearRarity.EPIC, StatKind.MAGIC_DEFENSE));
        bag.add(piece(GearSlot.RING, GearSet.SAGE, GearRarity.EPIC, StatKind.HP));

        SimGearAllocator.outfit(List.of(tank, sion), bag);

        assertEquals(GearSet.FOUR_PIECE, SetBonuses.count(tank.getLoadout(), GearSet.BULWARK));
        assertEquals(GearSet.TWO_PIECE, SetBonuses.count(sion.getLoadout(), GearSet.SAGE));
    }

    @Test
    public void upgradesPreferredSetPiecesWithSpareGold() {
        Character knight = PlayableCharacters.knight().createInstance();
        Inventory bag = new Inventory();
        bag.addGold(500);
        Gear helm = piece(GearSlot.HELM, GearSet.BULWARK, GearRarity.COMMON, StatKind.HP);
        bag.add(helm);
        SimGearAllocator.camp(List.of(knight), bag, new RandomProvider(1L));
        assertEquals(helm, knight.getLoadout().get(GearSlot.HELM));
        assertTrue(helm.getLevel() > 0);
        assertTrue(bag.getGold() < 500);
    }

    private static Gear piece(GearSlot slot, GearSet set, GearRarity rarity, StatKind main) {
        return new Gear(slot, set, rarity, main, 0, Gear.mainStatAt(main, 0), List.of());
    }
}
