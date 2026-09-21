package com.battlesim;

import com.battlesim.content.PlayableCharacters;
import com.battlesim.dungeon.Dungeon;
import com.battlesim.dungeon.DungeonResult;
import com.battlesim.dungeon.DungeonRun;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.Battle;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.SimpleAiMoveSelector;
import com.battlesim.model.Character;
import com.battlesim.model.CharacterTemplate;
import com.battlesim.model.Move;
import com.battlesim.model.Team;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final int TEAM_SLOTS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RandomProvider random = new RandomProvider();
        MoveSelector consoleSelector = consoleSelector(scanner);
        MoveSelector ai = new SimpleAiMoveSelector(random);

        System.out.println("=== RPG Battle Sim ===");
        System.out.println("1. PvP (you vs AI team)");
        System.out.println("2. Dungeon climb");
        int mode = readChoice(scanner, "Mode", 1, 2);

        List<Character> playerTeam = pickTeam(scanner, "your team", TEAM_SLOTS);
        if (playerTeam.isEmpty()) {
            System.out.println("Need at least one character.");
            scanner.close();
            return;
        }

        if (mode == 1) {
            List<Character> enemyTeam = pickTeam(scanner, "the AI team", TEAM_SLOTS);
            if (enemyTeam.isEmpty()) {
                System.out.println("Need at least one enemy.");
                scanner.close();
                return;
            }
            System.out.println();
            System.out.println("You control Team A. The AI plays Team B.");
            Battle.create(
                    new Team(playerTeam), new Team(enemyTeam),
                    consoleSelector, ai, random, Battle.DEFAULT_MAX_ACTIONS, true).run();
        } else {
            System.out.println();
            System.out.println("Dungeon: " + Dungeon.DEFAULT_FLOORS + " floors, boss every "
                    + Dungeon.BOSS_EVERY + ". Party of " + playerTeam.size()
                    + " (enemy stats x" + Dungeon.partySizeScale(playerTeam.size()) + ").");
            DungeonResult result = DungeonRun.run(
                    playerTeam, Dungeon.standard(random), consoleSelector, random, false, true);
            System.out.println();
            if (result.clearedAll()) {
                System.out.println("Cleared all " + result.getWavesCleared() + " waves!");
            } else {
                System.out.println("Wiped on wave " + (result.getWavesCleared() + 1)
                        + " (" + result.getWavesCleared() + " cleared).");
            }
        }

        scanner.close();
    }

    private static List<Character> pickTeam(Scanner scanner, String label, int slots) {
        List<CharacterTemplate> roster = PlayableCharacters.all();
        List<Character> team = new ArrayList<>();
        Set<String> taken = new HashSet<>();

        System.out.println();
        System.out.println("Pick " + label + " (" + slots + " slots, 0 = leave empty):");
        for (int i = 0; i < roster.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + roster.get(i).getName());
        }

        for (int slot = 1; slot <= slots; slot++) {
            while (true) {
                int choice = readChoice(scanner, "Slot " + slot + " (0 = empty)", 0, roster.size());
                if (choice == 0) {
                    System.out.println("  (empty)");
                    break;
                }
                CharacterTemplate template = roster.get(choice - 1);
                if (taken.contains(template.getName())) {
                    System.out.println("Already on this team.");
                    continue;
                }
                taken.add(template.getName());
                team.add(template.createInstance());
                System.out.println("  -> " + template.getName());
                break;
            }
        }
        return team;
    }

    private static MoveSelector consoleSelector(Scanner scanner) {
        return (actor, availableMoves, enemies, allies) -> {
            System.out.println();
            System.out.println(actor.getName() + " ("
                    + actor.getStats().getCurrentHp() + "/"
                    + actor.getStats().getMaxHp() + ")");
            List<Move> moves = availableMoves.isEmpty() ? actor.getMoves() : availableMoves;
            for (int i = 0; i < moves.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + moves.get(i).getName());
            }
            int moveChoice = readChoice(scanner, "Move", 1, moves.size()) - 1;
            Move chosenMove = moves.get(moveChoice);

            List<Character> pool = chosenMove.legalTargets(actor, allies, enemies);
            Character chosenTarget;
            if (chosenMove.targetsSelfOnly() || pool.isEmpty()) {
                chosenTarget = actor;
            } else if (pool.size() == 1) {
                chosenTarget = pool.get(0);
            } else {
                System.out.println(chosenMove.targetsAllies() ? "Choose an ally:" : "Choose a target:");
                for (int i = 0; i < pool.size(); i++) {
                    Character candidate = pool.get(i);
                    System.out.println("  " + (i + 1) + ". " + candidate.getName()
                            + " (" + candidate.getStats().getCurrentHp()
                            + "/" + candidate.getStats().getMaxHp() + ")");
                }
                chosenTarget = pool.get(readChoice(scanner, "Target", 1, pool.size()) - 1);
            }
            return new ActionChoice(chosenMove, List.of(chosenTarget));
        };
    }

    private static int readChoice(Scanner scanner, String prompt, int min, int max) {
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
