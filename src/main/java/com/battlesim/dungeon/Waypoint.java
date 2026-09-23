package com.battlesim.dungeon;

import com.battlesim.item.DropTable;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.Inventory;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Mid-dungeon rest stop. Console play picks; sims rest when someone is
 * fainted or under {@link #REST_BELOW_HP_FRACTION}, otherwise take the chest.
 */
public final class Waypoint {

    public static final double REST_BELOW_HP_FRACTION = 0.70;

    private Waypoint() {
    }

    public static boolean isDue(int waveNumber, boolean moreWaves) {
        return moreWaves && Dungeon.isWaypointWave(waveNumber);
    }

    public static WaypointChoice autoPick(List<Character> party) {
        return needsRest(party) ? WaypointChoice.REST : WaypointChoice.CHEST;
    }

    public static boolean needsRest(List<Character> party) {
        if (party == null) {
            return false;
        }
        for (Character member : party) {
            if (member == null || member.isSummon()) {
                continue;
            }
            if (member.isFainted()) {
                return true;
            }
            int maxHp = member.getStats().getMaxHp();
            if (maxHp > 0 && member.getStats().getCurrentHp() < maxHp * REST_BELOW_HP_FRACTION) {
                return true;
            }
        }
        return false;
    }

    public static List<String> apply(WaypointChoice choice,
                                     List<Character> party,
                                     Inventory bag,
                                     int waveNumber,
                                     double scale,
                                     RandomProvider random) {
        if (choice == WaypointChoice.CHEST) {
            return openChest(bag, waveNumber, scale, random);
        }
        return rest(party);
    }

    public static List<String> rest(List<Character> party) {
        List<String> log = new ArrayList<>();
        if (party == null) {
            return log;
        }
        for (Character member : party) {
            if (member == null || member.isSummon()) {
                continue;
            }
            boolean revived = member.isFainted();
            member.restFully();
            log.add(member.getName() + (revived ? " is revived and fully rested" : " is fully rested"));
        }
        return log;
    }

    public static List<String> openChest(Inventory bag,
                                         int waveNumber,
                                         double scale,
                                         RandomProvider random) {
        List<String> log = new ArrayList<>();
        DropTable.Loot loot = DropTable.chest(waveNumber, scale, random);
        if (bag != null && loot.getGold() > 0) {
            bag.addGold(loot.getGold());
            log.add("Chest: " + loot.getGold() + " gold (total " + bag.getGold() + ")");
        }
        for (Gear drop : loot.getDrops()) {
            if (bag != null) {
                bag.add(drop);
            }
            log.add("Chest: " + drop.describe());
        }
        if (log.isEmpty()) {
            log.add("The chest is empty");
        }
        return log;
    }
}
