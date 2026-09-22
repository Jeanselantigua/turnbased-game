package com.battlesim.dungeon;

import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearSlot;
import com.battlesim.item.SetBonuses;
import com.battlesim.model.Inventory;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.util.RandomProvider;
import java.util.List;
import java.util.Scanner;

/** Console between-wave menu: inspect, equip, enhance, leftover move points. */
public final class ConsoleCamp implements Camp {

    private final Scanner scanner;
    private final RandomProvider random;

    public ConsoleCamp(Scanner scanner, RandomProvider random) {
        this.scanner = scanner;
        this.random = random;
    }

    @Override
    public void afterWave(List<Character> party, Inventory inventory, int waveNumber, boolean moreWaves) {
        if (party == null || party.isEmpty() || inventory == null) {
            return;
        }
        while (true) {
            System.out.println();
            System.out.println("--- Camp (wave " + waveNumber + " cleared) ---");
            System.out.println("Gold: " + inventory.getGold()
                    + "  Bag: " + inventory.size()
                    + (moreWaves ? "" : "  (last wave)"));
            System.out.println("  1. Continue");
            System.out.println("  2. Character menu");
            System.out.println("  3. View bag");
            int choice = readChoice("Camp", 1, 3);
            if (choice == 1) {
                return;
            }
            if (choice == 3) {
                printBag(inventory);
                continue;
            }
            Character member = pickCharacter(party);
            if (member != null) {
                characterMenu(member, inventory);
            }
        }
    }

    private void characterMenu(Character character, Inventory inventory) {
        while (true) {
            printSheet(character, inventory);
            System.out.println("  1. Equip from bag");
            System.out.println("  2. Unequip");
            System.out.println("  3. Upgrade equipped");
            System.out.println("  4. Upgrade from bag");
            System.out.println("  5. Spend move points");
            System.out.println("  0. Back");
            int choice = readChoice("Action", 0, 5);
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                equipFromBag(character, inventory);
            } else if (choice == 2) {
                unequipSlot(character, inventory);
            } else if (choice == 3) {
                upgradeEquipped(character, inventory);
            } else if (choice == 4) {
                upgradeFromBag(character, inventory);
            } else {
                spendMovePoints(character);
            }
        }
    }

    private void equipFromBag(Character character, Inventory inventory) {
        if (inventory.isEmpty()) {
            System.out.println("Bag is empty.");
            return;
        }
        printBag(inventory);
        int choice = readChoice("Equip (0 = cancel)", 0, inventory.size());
        if (choice == 0) {
            return;
        }
        Gear piece = inventory.get(choice - 1);
        if (character.equip(piece, inventory)) {
            System.out.println("  -> equipped " + piece.summary());
        } else {
            System.out.println("Could not equip that piece.");
        }
    }

    private void unequipSlot(Character character, Inventory inventory) {
        GearSlot[] slots = GearSlot.values();
        boolean any = false;
        for (int i = 0; i < slots.length; i++) {
            Gear piece = character.getLoadout().get(slots[i]);
            if (piece != null) {
                any = true;
                System.out.println("  " + (i + 1) + ". " + piece.describe());
            } else {
                System.out.println("  " + (i + 1) + ". " + slots[i].getLabel() + "  (empty)");
            }
        }
        if (!any) {
            System.out.println("Nothing equipped.");
            return;
        }
        int choice = readChoice("Unequip slot (0 = cancel)", 0, slots.length);
        if (choice == 0) {
            return;
        }
        Gear removed = character.unequip(slots[choice - 1], inventory);
        if (removed == null) {
            System.out.println("That slot is empty.");
        } else {
            System.out.println("  -> bagged " + removed.summary());
        }
    }

    private void upgradeEquipped(Character character, Inventory inventory) {
        GearSlot[] slots = GearSlot.values();
        boolean any = false;
        for (int i = 0; i < slots.length; i++) {
            Gear piece = character.getLoadout().get(slots[i]);
            if (piece != null) {
                any = true;
                int cost = Gear.upgradeCost(piece.getLevel());
                System.out.println("  " + (i + 1) + ". " + piece.describe()
                        + (cost > 0 ? "  (" + cost + " gold)" : "  (max)"));
            }
        }
        if (!any) {
            System.out.println("Nothing equipped.");
            return;
        }
        int choice = readChoice("Upgrade slot (0 = cancel)", 0, slots.length);
        if (choice == 0) {
            return;
        }
        Gear piece = character.getLoadout().get(slots[choice - 1]);
        tryUpgrade(character, piece, inventory);
    }

    private void upgradeFromBag(Character character, Inventory inventory) {
        if (inventory.isEmpty()) {
            System.out.println("Bag is empty.");
            return;
        }
        for (int i = 0; i < inventory.size(); i++) {
            Gear piece = inventory.get(i);
            int cost = Gear.upgradeCost(piece.getLevel());
            System.out.println("  " + (i + 1) + ". " + piece.describe()
                    + (cost > 0 ? "  (" + cost + " gold)" : "  (max)"));
        }
        int choice = readChoice("Upgrade (0 = cancel)", 0, inventory.size());
        if (choice == 0) {
            return;
        }
        tryUpgrade(character, inventory.get(choice - 1), inventory);
    }

    private void tryUpgrade(Character character, Gear piece, Inventory inventory) {
        if (piece == null) {
            System.out.println("Nothing there.");
            return;
        }
        if (piece.getLevel() >= Gear.MAX_LEVEL) {
            System.out.println("Already +" + Gear.MAX_LEVEL + ".");
            return;
        }
        int cost = Gear.upgradeCost(piece.getLevel());
        if (inventory.getGold() < cost) {
            System.out.println("Need " + cost + " gold.");
            return;
        }
        if (!character.upgradeGear(piece, inventory, random)) {
            System.out.println("Could not upgrade.");
            return;
        }
        System.out.println("  -> " + piece.describe());
    }

    private void spendMovePoints(Character character) {
        while (character.getUnspentMovePoints() > 0) {
            List<Move> known = character.getMoves();
            if (known.isEmpty()) {
                return;
            }
            System.out.println();
            System.out.println(character.getName() + "  move points: " + character.getUnspentMovePoints()
                    + "  (0 = bank the rest)");
            for (int i = 0; i < known.size(); i++) {
                Move move = known.get(i);
                System.out.println("  " + (i + 1) + ". " + move.getName()
                        + "  rank " + character.getMoveRank(move)
                        + "  power " + character.effectivePower(move)
                        + "  scale x" + String.format("%.2f", character.moveScaling(move)));
            }
            int choice = readChoice("Move", 0, known.size());
            if (choice == 0) {
                return;
            }
            Move move = known.get(choice - 1);
            int rank = character.spendMovePoint(move);
            System.out.println("  -> " + move.getName() + " is now rank " + rank);
        }
        if (character.getUnspentMovePoints() == 0) {
            System.out.println("No move points left.");
        }
    }

    private Character pickCharacter(List<Character> party) {
        System.out.println("Who? (0 = back)");
        for (int i = 0; i < party.size(); i++) {
            Character member = party.get(i);
            System.out.println("  " + (i + 1) + ". " + member.getName()
                    + "  Lv " + member.getLevel()
                    + "  " + member.getStats().getCurrentHp() + "/" + member.getStats().getMaxHp());
        }
        int choice = readChoice("Character", 0, party.size());
        if (choice == 0) {
            return null;
        }
        return party.get(choice - 1);
    }

    private static void printBag(Inventory inventory) {
        if (inventory.isEmpty()) {
            System.out.println("Bag is empty. Gold: " + inventory.getGold());
            return;
        }
        System.out.println("Bag (" + inventory.size() + ")  gold " + inventory.getGold() + ":");
        for (int i = 0; i < inventory.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + inventory.get(i).describe());
        }
    }

    private static void printSheet(Character character, Inventory inventory) {
        Stats stats = character.getStats();
        System.out.println();
        System.out.println(character.getName() + "  Lv " + character.getLevel()
                + "  gold " + inventory.getGold()
                + "  move pts " + character.getUnspentMovePoints());
        System.out.println("  HP " + stats.getCurrentHp() + "/" + stats.getMaxHp()
                + "  ATK " + stats.getAttack()
                + "  DEF " + stats.getDefense()
                + "  MATK " + stats.getMagicAttack()
                + "  MDEF " + stats.getMagicDefense()
                + "  SPD " + stats.getSpeed()
                + "  CRIT " + character.getCritRate() + "%"
                + "  CDMG " + character.getCritDamage() + "%");
        String sets = SetBonuses.summary(character.getLoadout());
        if (!sets.isEmpty()) {
            System.out.println("  Sets: " + sets);
        }
        for (GearSlot slot : GearSlot.values()) {
            Gear piece = character.getLoadout().get(slot);
            System.out.println("  " + slot.getLabel() + ": "
                    + (piece == null ? "(empty)" : piece.describe()));
        }
    }

    private int readChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " [" + min + "-" + max + "]: ");
            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("Enter a number.");
                continue;
            }
            int value = scanner.nextInt();
            if (value < min || value > max) {
                System.out.println("Enter " + min + " to " + max + ".");
                continue;
            }
            return value;
        }
    }
}
