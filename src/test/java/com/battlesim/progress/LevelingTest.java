package com.battlesim.progress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.StatKind;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Progression;
import com.battlesim.model.Type;
import java.util.List;
import org.junit.Test;

public class LevelingTest {

    @Test
    public void levelUpAlwaysAddsASmallAmountOfHpAndSpeed() {
        Character hero = PlayableCharacters.rogue().createInstance();
        int startHp = hero.getStats().getMaxHp();
        int startSpd = hero.getStats().getSpeed();
        int startAtk = hero.getStats().getAttack();
        hero.grantXp(100);
        assertEquals(2, hero.getLevel());
        assertEquals(startHp + Growth.AUTO_HP_PER_LEVEL, hero.getStats().getMaxHp());
        assertEquals(startSpd + Growth.AUTO_SPEED_PER_LEVEL, hero.getStats().getSpeed());
        assertEquals(startAtk, hero.getStats().getAttack());
        assertEquals(Progression.STAT_POINTS_PER_LEVEL, hero.getUnspentStatPoints());
    }

    @Test
    public void skippingHpPointsStillGrowsHpButStaysFragile() {
        Character skipHp = PlayableCharacters.rogue().createInstance();
        Character investHp = PlayableCharacters.rogue().createInstance();
        int startHp = skipHp.getStats().getMaxHp();
        int startSpd = skipHp.getStats().getSpeed();
        skipHp.grantXp(100);
        investHp.grantXp(100);
        while (skipHp.getUnspentStatPoints() > 0) {
            skipHp.spendStatPoint(StatKind.ATTACK);
        }
        while (investHp.getUnspentStatPoints() > 0) {
            investHp.spendStatPoint(StatKind.HP);
        }
        assertTrue(investHp.getStats().getMaxHp() > skipHp.getStats().getMaxHp());
        assertEquals(startHp + Growth.AUTO_HP_PER_LEVEL, skipHp.getStats().getMaxHp());
        assertEquals(startSpd + Growth.AUTO_SPEED_PER_LEVEL, skipHp.getStats().getSpeed());
    }

    @Test
    public void skippingSpeedPointsStillGrowsSpeed() {
        Character skipSpd = PlayableCharacters.rogue().createInstance();
        Character investSpd = PlayableCharacters.rogue().createInstance();
        int startSpd = skipSpd.getStats().getSpeed();
        skipSpd.grantXp(100);
        investSpd.grantXp(100);
        while (skipSpd.getUnspentStatPoints() > 0) {
            skipSpd.spendStatPoint(StatKind.ATTACK);
        }
        while (investSpd.getUnspentStatPoints() > 0) {
            investSpd.spendStatPoint(StatKind.SPEED);
        }
        assertTrue(investSpd.getStats().getSpeed() > skipSpd.getStats().getSpeed());
        assertEquals(startSpd + Growth.AUTO_SPEED_PER_LEVEL, skipSpd.getStats().getSpeed());
    }

    @Test
    public void specialtyPointsAreWorthMore() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int startAtk = rogue.getStats().getAttack();
        int startMatk = rogue.getStats().getMagicAttack();
        rogue.grantXp(Progression.xpToReachLevel(3));
        rogue.spendStatPoint(StatKind.ATTACK);
        rogue.spendStatPoint(StatKind.MAGIC_ATTACK);
        assertEquals(startAtk + Growth.COMBAT_PER_SPECIALTY_POINT, rogue.getStats().getAttack());
        assertEquals(startMatk + Growth.COMBAT_PER_POINT, rogue.getStats().getMagicAttack());
    }

    @Test
    public void faintedCharactersDoNotReviveFromTheHpBump() {
        Character hero = PlayableCharacters.knight().createInstance();
        hero.getStats().applyDamage(hero.getStats().getMaxHp());
        assertTrue(hero.isFainted());
        hero.grantXp(100);
        assertEquals(0, hero.getStats().getCurrentHp());
        assertTrue(hero.isFainted());
        assertEquals(120 + Growth.AUTO_HP_PER_LEVEL, hero.getStats().getMaxHp());
    }

    @Test
    public void summonsDoNotGainXp() {
        Character chicken = new Character("Chicken Spirit",
                new Stats(40, 15, 8, 5, 8, 38), Type.UNDEAD,
                List.of(new Move("Peck", Type.UNDEAD, 25, 100, 0, false, Status.NONE, 0)));
        chicken.setSummoner(new Character("Chef", new Stats(10, 1, 1, 1, 1, 1),
                Type.UNDEAD, List.of()));
        assertEquals(0, chicken.grantXp(500));
        assertEquals(1, chicken.getLevel());
    }

    @Test
    public void evenAllocatorSpreadsPointsAcrossEveryStat() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int startHp = rogue.getStats().getMaxHp();
        int startAtk = rogue.getStats().getAttack();
        int startDef = rogue.getStats().getDefense();
        int startSpd = rogue.getStats().getSpeed();
        rogue.grantXp(Progression.xpToReachLevel(7));
        new EvenStatAllocator().allocate(rogue);
        int levelsGained = 6;
        assertEquals(0, rogue.getUnspentStatPoints());
        assertEquals(startHp + levelsGained * Growth.AUTO_HP_PER_LEVEL + Growth.HP_PER_POINT,
                rogue.getStats().getMaxHp());
        assertEquals(startAtk + Growth.COMBAT_PER_SPECIALTY_POINT, rogue.getStats().getAttack());
        assertEquals(startDef + Growth.COMBAT_PER_POINT, rogue.getStats().getDefense());
        assertEquals(startSpd + levelsGained * Growth.AUTO_SPEED_PER_LEVEL + Growth.SPEED_PER_SPECIALTY_POINT,
                rogue.getStats().getSpeed());
    }

    @Test
    public void evenAllocatorContinuesTheCycleAcrossLevelUps() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int startHp = rogue.getStats().getMaxHp();
        int startAtk = rogue.getStats().getAttack();
        rogue.grantXp(100);
        new EvenStatAllocator().allocate(rogue);
        assertEquals(startHp + Growth.AUTO_HP_PER_LEVEL + Growth.HP_PER_POINT, rogue.getStats().getMaxHp());
        assertEquals(startAtk, rogue.getStats().getAttack());
        rogue.grantXp(200);
        new EvenStatAllocator().allocate(rogue);
        assertEquals(startAtk + Growth.COMBAT_PER_SPECIALTY_POINT, rogue.getStats().getAttack());
    }

    @Test
    public void evenAllocatorPutsExtraPointsIntoSpecialtyStats() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int startAtk = rogue.getStats().getAttack();
        int startDef = rogue.getStats().getDefense();
        int startMatk = rogue.getStats().getMagicAttack();
        int startSpd = rogue.getStats().getSpeed();
        rogue.grantXp(Progression.xpToReachLevel(Progression.MAX_LEVEL));
        new EvenStatAllocator().allocate(rogue);
        int atkGain = rogue.getStats().getAttack() - startAtk;
        int defGain = rogue.getStats().getDefense() - startDef;
        int matkGain = rogue.getStats().getMagicAttack() - startMatk;
        int spdGain = rogue.getStats().getSpeed() - startSpd;
        assertTrue("ATK specialty should outgrow DEF", atkGain > defGain);
        assertTrue("ATK specialty should outgrow MATK", atkGain > matkGain);
        assertTrue("SPD specialty should outgrow DEF", spdGain > defGain);
    }

    @Test
    public void dungeonStatPointsCanAllGoIntoOneStat() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        int startAtk = rogue.getStats().getAttack();
        rogue.grantXp(Progression.xpToReachLevel(Progression.MAX_LEVEL));
        while (rogue.getUnspentStatPoints() > 0) {
            rogue.spendStatPoint(StatKind.ATTACK);
        }
        assertEquals(0, rogue.getUnspentStatPoints());
        assertTrue(rogue.getStats().getAttack() > startAtk);
    }

    @Test
    public void rogueLearnsMovesOnTheStandardLadder() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        assertEquals("BackStab", rogue.getMoves().get(0).getName());

        rogue.grantXp(1500); // exactly level 6
        assertEquals(6, rogue.getLevel());
        assertEquals(2, rogue.getMoves().size());
        assertTrue(rogue.knowsMove("Ambush"));

        rogue.grantXp(5100); // 200+...+1100 = 5100 more → level 12
        assertEquals(12, rogue.getLevel());
        assertTrue(rogue.knowsMove("Maim"));

        rogue.grantXp(8700); // 1200+...+1700 = 8700 more → level 18
        assertEquals(18, rogue.getLevel());
        assertTrue(rogue.knowsMove("Assassinate"));
        assertEquals(4, rogue.getMoves().size());
        assertEquals(0, rogue.getUnspentMovePoints());
    }

    @Test
    public void movePointsRaisePowerAndScalingOnTheUlt() {
        Character rogue = PlayableCharacters.rogue().createInstance();
        rogue.grantXp(17100); // level 19, first move point
        assertEquals(19, rogue.getLevel());
        assertEquals(1, rogue.getUnspentMovePoints());
        Move ult = rogue.getKit().getUlt();
        int basePower = ult.getPower();
        new EvenStatAllocator().allocate(rogue);
        assertEquals(0, rogue.getUnspentMovePoints());
        assertEquals(1, rogue.getMoveRank(ult));
        assertEquals(basePower + Growth.POWER_PER_MOVE_POINT, rogue.effectivePower(ult));
        assertEquals(1.0 + Growth.SCALING_PER_MOVE_POINT, rogue.moveScaling(ult), 0.0001);
    }
}
