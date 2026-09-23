package com.battlesim.dungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.battlesim.content.Enemies;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.item.DropTable;
import com.battlesim.model.Character;
import com.battlesim.model.GearRarity;
import com.battlesim.model.Inventory;
import com.battlesim.model.Status;
import com.battlesim.progress.EvenStatAllocator;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class WaypointTest {

    @Test
    public void waypointsFallOnTensIfTheClimbContinues() {
        assertFalse(Waypoint.isDue(9, true));
        assertTrue(Waypoint.isDue(10, true));
        assertFalse(Waypoint.isDue(10, false));
        assertTrue(Waypoint.isDue(20, true));
        assertFalse(Waypoint.isDue(100, false));
    }

    @Test
    public void restRevivesClearsStatusAndRefillsHp() {
        Character knight = PlayableCharacters.knight().createInstance();
        knight.getStats().applyDamage(knight.getStats().getMaxHp());
        knight.applyStatus(Status.POISON, 10);
        knight.startMoveCooldown("Slash", 2);
        assertTrue(knight.isFainted());

        List<String> log = Waypoint.rest(List.of(knight));

        assertFalse(knight.isFainted());
        assertEquals(knight.getStats().getMaxHp(), knight.getStats().getCurrentHp());
        assertFalse(knight.hasAnyStatus());
        assertEquals(0, knight.getMoveCooldown("Slash"));
        assertTrue(log.get(0).contains("revived"));
    }

    @Test
    public void autoPickRestsWhenSomeoneIsDownOrLow() {
        Character healthy = PlayableCharacters.knight().createInstance();
        assertEquals(WaypointChoice.CHEST, Waypoint.autoPick(List.of(healthy)));

        Character hurt = PlayableCharacters.rogue().createInstance();
        hurt.getStats().applyDamage((int) Math.round(hurt.getStats().getMaxHp() * 0.40));
        assertEquals(WaypointChoice.REST, Waypoint.autoPick(List.of(healthy, hurt)));

        Character down = PlayableCharacters.caveman().createInstance();
        down.getStats().applyDamage(down.getStats().getMaxHp());
        assertEquals(WaypointChoice.REST, Waypoint.autoPick(List.of(healthy, down)));
    }

    @Test
    public void chestAlwaysDropsGoldAndAPiece() {
        Inventory bag = new Inventory();
        RandomProvider random = new RandomProvider(1L);
        List<String> log = Waypoint.openChest(bag, 10, 1.0, random);
        assertEquals(DropTable.GOLD_ELITE * 2, bag.getGold());
        assertEquals(1, bag.size());
        assertTrue(bag.get(0).getRarity() == GearRarity.RARE
                || bag.get(0).getRarity() == GearRarity.EPIC
                || bag.get(0).getRarity() == GearRarity.LEGENDARY);
        assertFalse(log.isEmpty());
    }

    @Test
    public void dungeonRunOffersWaypointsAtTenAndTwenty() {
        List<Integer> floors = new ArrayList<>();
        Camp camp = new Camp() {
            @Override
            public void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves) {
            }

            @Override
            public WaypointChoice pickWaypoint(List<Character> party, Inventory inventory, int waveNumber) {
                floors.add(waveNumber);
                return WaypointChoice.REST;
            }
        };
        List<Wave> waves = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            waves.add(new Wave(List.of(Enemies.iceSlime())));
        }
        Character knight = PlayableCharacters.knight().createInstance();
        RandomProvider random = new RandomProvider(4L);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(random);
        DungeonResult result = DungeonRun.run(
                List.of(knight), new Dungeon(waves, 0.15), ai, random, false, false,
                new EvenStatAllocator(), new Inventory(), camp);

        assertTrue(result.getWavesCleared() >= 20);
        assertEquals(List.of(10, 20), floors);
    }
}
