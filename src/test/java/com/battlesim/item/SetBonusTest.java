package com.battlesim.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.sets.BulwarkFourPiecePassive;
import com.battlesim.content.sets.SageFourPiecePassive;
import com.battlesim.content.sets.SwiftFourPiecePassive;
import com.battlesim.content.sets.VampireFourPiecePassive;
import com.battlesim.content.sets.VampireTwoPiecePassive;
import com.battlesim.content.sets.WarlordFourPiecePassive;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearRarity;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.Inventory;
import com.battlesim.model.Move;
import com.battlesim.model.StatKind;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SetBonusTest {

    private static final Move SLASH = new Move("Slash", Type.PHYSICAL, 40, 100, 0, false, Status.NONE, 0);
    private static final Move BOLT = new Move("Bolt", Type.LIGHTNING, 40, 100, 0, true, Status.NONE, 0);
    private static final GearSlot[] FOUR_SLOTS = {
            GearSlot.HELM, GearSlot.GLOVES, GearSlot.CHEST, GearSlot.BOOTS
    };

    @Test
    public void onePieceDoesNotGrantTwoPieceStats() {
        Character hero = fighter(100, 50, 20, 30, 20, 40);
        int atk = hero.getStats().getAttack();
        equip(hero, piece(GearSlot.GLOVES, GearSet.WARLORD));
        assertEquals(atk + hero.getLoadout().get(GearSlot.GLOVES).bonus(StatKind.ATTACK),
                hero.getStats().getAttack());
        assertEquals(0, hero.getLoadout().twoPieceBonus(StatKind.ATTACK));
    }

    @Test
    public void twoWarlordPiecesAddTwentyPercentAttackAndUnequipRemovesIt() {
        Character hero = fighter(100, 50, 20, 30, 20, 40);
        Gear helm = piece(GearSlot.HELM, GearSet.WARLORD);
        Gear gloves = piece(GearSlot.GLOVES, GearSet.WARLORD);
        int basePlusGear = 50 + helm.bonus(StatKind.ATTACK) + gloves.bonus(StatKind.ATTACK);
        equip(hero, helm, gloves);
        int expectedBonus = SetBonuses.percentOf(basePlusGear, SetBonuses.TWO_PIECE_PERCENT);
        assertEquals(2, hero.getLoadout().count(GearSet.WARLORD));
        assertEquals(expectedBonus, hero.getLoadout().twoPieceBonus(StatKind.ATTACK));
        assertEquals(basePlusGear + expectedBonus, hero.getStats().getAttack());

        hero.unequip(GearSlot.GLOVES, new Inventory());
        assertEquals(0, hero.getLoadout().twoPieceBonus(StatKind.ATTACK));
        assertEquals(50 + helm.bonus(StatKind.ATTACK), hero.getStats().getAttack());
    }

    @Test
    public void warlordFourPieceScalesAttackWithFallenAllies() {
        Character hero = fighter(100, 50, 20, 0, 10, 20);
        Character allyA = fighter(80, 10, 10, 0, 10, 10);
        Character allyB = fighter(80, 10, 10, 0, 10, 10);
        equipSet(hero, GearSet.WARLORD);
        assertTrue(hero.hasPassive(WarlordFourPiecePassive.class));
        int base = SetBonuses.attackWithoutWarlordSet(hero, hero.getPassive(WarlordFourPiecePassive.class));
        BattleContext ctx = party(hero, allyA, allyB);

        hero.getPassive(WarlordFourPiecePassive.class).onFieldChanged(hero, ctx, new ArrayList<>());
        assertEquals(0, hero.getPassive(WarlordFourPiecePassive.class).getAppliedAttack());

        allyA.getStats().applyDamage(allyA.getStats().getMaxHp());
        hero.getPassive(WarlordFourPiecePassive.class).onFieldChanged(hero, ctx, new ArrayList<>());
        assertEquals(SetBonuses.percentOf(base, SetBonuses.WARLORD_ONE_DEAD_ATTACK),
                hero.getPassive(WarlordFourPiecePassive.class).getAppliedAttack());

        allyB.getStats().applyDamage(allyB.getStats().getMaxHp());
        hero.getPassive(WarlordFourPiecePassive.class).onFieldChanged(hero, ctx, new ArrayList<>());
        assertEquals(SetBonuses.percentOf(base, SetBonuses.WARLORD_TWO_DEAD_ATTACK),
                hero.getPassive(WarlordFourPiecePassive.class).getAppliedAttack());
    }

    @Test
    public void sageFourPieceBuffsAllySpeedBelowSeventyFivePercentHp() {
        Character sage = fighter(200, 10, 10, 40, 10, 20);
        Character ally = fighter(100, 10, 10, 10, 10, 15);
        equipSet(sage, GearSet.SAGE);
        int allySpeed = ally.getStats().getSpeed();
        BattleContext ctx = party(sage, ally);
        sage.getStats().applyDamage(60); // 140/200 = 70%
        sage.getPassive(SageFourPiecePassive.class)
                .onDamageTaken(sage, ally, 60, new ArrayList<>(), ctx);
        assertEquals(allySpeed + SetBonuses.SAGE_ALLY_SPEED_LOW, ally.getStats().getSpeed());

        sage.getStats().applyDamage(50); // 90/200 = 45%
        sage.getPassive(SageFourPiecePassive.class)
                .onDamageTaken(sage, ally, 50, new ArrayList<>(), ctx);
        assertEquals(allySpeed + SetBonuses.SAGE_ALLY_SPEED_HIGH, ally.getStats().getSpeed());

        sage.unequip(GearSlot.HELM, new Inventory());
        assertEquals(allySpeed, ally.getStats().getSpeed());
    }

    @Test
    public void swiftFourPieceAddsCritDamageOverSpeedTiers() {
        Character swift = fighter(100, 40, 10, 10, 10, 55);
        equipSet(swift, GearSet.SWIFT);
        assertTrue(swift.hasPassive(SwiftFourPiecePassive.class));
        assertTrue(swift.getStats().getSpeed() > SetBonuses.SWIFT_SPEED_TIER_1);
        assertTrue(swift.getStats().getSpeed() <= SetBonuses.SWIFT_SPEED_TIER_2);
        assertEquals(20, swift.getCritDamage());
        assertEquals(0, swift.getCritRate());
    }

    @Test
    public void bulwarkFourPieceGrantsAShieldBelowHalfHealth() {
        Character tank = fighter(200, 10, 40, 0, 20, 10);
        equipSet(tank, GearSet.BULWARK);
        assertTrue(tank.hasPassive(BulwarkFourPiecePassive.class));
        tank.getStats().applyDamage(110);
        tank.getPassive(BulwarkFourPiecePassive.class)
                .onDamageTaken(tank, tank, 110, new ArrayList<>());
        int expected = SetBonuses.percentOf(tank.getStats().getMaxHp(), SetBonuses.BULWARK_SHIELD_PERCENT);
        assertEquals(expected, tank.getStats().getShieldHp());
    }

    @Test
    public void vampireTwoPieceHealsFromPhysicalHitsOnly() {
        Character vamp = fighter(200, 40, 10, 10, 10, 20);
        vamp.getStats().applyDamage(80);
        equip(vamp, piece(GearSlot.HELM, GearSet.VAMPIRE), piece(GearSlot.GLOVES, GearSet.VAMPIRE));
        assertTrue(vamp.hasPassive(VampireTwoPiecePassive.class));
        int hp = vamp.getStats().getCurrentHp();
        vamp.getPassive(VampireTwoPiecePassive.class)
                .onHitLanded(vamp, fighter(50, 1, 1, 1, 1, 1), SLASH, 40, false, new ArrayList<>(), null);
        assertEquals(hp + 10, vamp.getStats().getCurrentHp());

        hp = vamp.getStats().getCurrentHp();
        vamp.getPassive(VampireTwoPiecePassive.class)
                .onHitLanded(vamp, fighter(50, 1, 1, 1, 1, 1), BOLT, 40, false, new ArrayList<>(), null);
        assertEquals(hp, vamp.getStats().getCurrentHp());
    }

    @Test
    public void vampireFourPieceGrowsMaxHpOnKill() {
        Character vamp = fighter(200, 40, 10, 10, 10, 20);
        equipSet(vamp, GearSet.VAMPIRE);
        assertTrue(vamp.hasPassive(VampireFourPiecePassive.class));
        int max = vamp.getStats().getMaxHp();
        vamp.getPassive(VampireFourPiecePassive.class)
                .onKill(vamp, fighter(10, 1, 1, 1, 1, 1), SLASH, null, new ArrayList<>());
        assertEquals(max + SetBonuses.percentOf(max, SetBonuses.VAMPIRE_KILL_MAX_HP),
                vamp.getStats().getMaxHp());
    }

    private static Character fighter(int hp, int atk, int def, int matk, int mdef, int spd) {
        return new Character("Hero", new Stats(hp, atk, def, matk, mdef, spd), Type.PHYSICAL, List.of());
    }

    private static Gear piece(GearSlot slot, GearSet set) {
        return GearFactory.create(slot, set, GearRarity.COMMON, new RandomProvider(1L));
    }

    private static void equipSet(Character character, GearSet set) {
        Gear[] pieces = new Gear[FOUR_SLOTS.length];
        for (int i = 0; i < FOUR_SLOTS.length; i++) {
            pieces[i] = piece(FOUR_SLOTS[i], set);
        }
        equip(character, pieces);
    }

    private static void equip(Character character, Gear... pieces) {
        Inventory bag = new Inventory();
        for (Gear piece : pieces) {
            bag.add(piece);
            character.equip(piece, bag);
        }
    }

    private static BattleContext party(Character... members) {
        List<Character> team = List.of(members);
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return team;
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return List.of();
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }
}
