package com.battlesim.progress;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.StatKind;
import com.battlesim.model.Stats;
import java.util.List;
import java.util.Scanner;

/** Lets the player spend level-up points in the console dungeon. */
public final class ConsoleStatAllocator implements StatAllocator {

    private final Scanner scanner;

    public ConsoleStatAllocator(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void allocate(Character character) {
        if (character == null) {
            return;
        }
        StatKind[] kinds = StatKind.pointStats();
        while (character.getUnspentStatPoints() > 0) {
            printSheet(character);
            System.out.println("Spend a stat point (0 = bank the rest):");
            for (int i = 0; i < kinds.length; i++) {
                StatKind kind = kinds[i];
                boolean specialty = character.getSpecialties().contains(kind);
                System.out.println("  " + (i + 1) + ". " + kind.getLabel()
                        + " +" + Growth.pointGain(kind, specialty)
                        + (specialty ? " (specialty)" : ""));
            }
            int choice = readChoice(0, kinds.length);
            if (choice == 0) {
                break;
            }
            StatKind kind = kinds[choice - 1];
            int gain = character.spendStatPoint(kind);
            System.out.println("  -> " + character.getName() + " +" + gain + " " + kind.getLabel());
        }
        if (character.getUnspentStatPoints() == 0) {
            printSheet(character);
        }
        allocateMovePoints(character);
    }

    private void allocateMovePoints(Character character) {
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
                int rank = character.getMoveRank(move);
                boolean ult = character.getKit() != null && character.getKit().getUlt() != null
                        && character.getKit().getUlt().getName().equals(move.getName());
                System.out.println("  " + (i + 1) + ". " + move.getName()
                        + "  rank " + rank
                        + "  power " + character.effectivePower(move)
                        + "  scale x" + String.format("%.2f", character.moveScaling(move))
                        + (ult ? " (ult)" : ""));
            }
            int choice = readMoveChoice(0, known.size());
            if (choice == 0) {
                return;
            }
            Move move = known.get(choice - 1);
            int rank = character.spendMovePoint(move);
            System.out.println("  -> " + move.getName() + " is now rank " + rank
                    + " (power " + character.effectivePower(move)
                    + ", scale x" + String.format("%.2f", character.moveScaling(move)) + ")");
        }
    }

    private int readMoveChoice(int min, int max) {
        while (true) {
            System.out.print("Move [" + min + "-" + max + "]: ");
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

    private static void printSheet(Character character) {
        Stats stats = character.getStats();
        System.out.println();
        System.out.println(character.getName() + "  Lv " + character.getLevel()
                + "  points: " + character.getUnspentStatPoints());
        System.out.println("  HP " + stats.getCurrentHp() + "/" + stats.getMaxHp()
                + "  ATK " + stats.getAttack()
                + "  DEF " + stats.getDefense()
                + "  MATK " + stats.getMagicAttack()
                + "  MDEF " + stats.getMagicDefense()
                + "  SPD " + stats.getSpeed()
                + "  CRIT " + character.getCritRate() + "%"
                + "  CDMG " + character.getCritDamage() + "%"
                + "  BST " + stats.baseStatTotal());
    }

    private int readChoice(int min, int max) {
        while (true) {
            System.out.print("Stat [" + min + "-" + max + "]: ");
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
