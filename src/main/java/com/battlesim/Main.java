package com.battlesim;

import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.Battle;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.StatusEffectResolver;
import com.battlesim.engine.TurnOrderScheduler;
import com.battlesim.engine.TurnResolver;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Team;
import com.battlesim.util.RandomProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Character definitions now live in content.PlayableCharacters —
        // Main just spawns fresh battle instances from those templates.
        Character knight = PlayableCharacters.knight().createInstance();
        Character rogue = PlayableCharacters.rogue().createInstance();
        Character monk = PlayableCharacters.monk().createInstance();
        Character caveman = PlayableCharacters.caveman().createInstance();

        Team playerTeam = new Team(List.of(rogue, monk));
        Team enemyTeam = new Team(List.of(knight, caveman));

        // Player-controlled: choose a move from console, then a target from console.
        MoveSelector consoleSelector = (actor, enemies) -> {
            System.out.println(actor.getName() + "'s moves:");
            List<Move> moves = actor.getMoves();
            for (int i = 0; i < moves.size(); i++) {
                System.out.println((i + 1) + ". " + moves.get(i).getName());
            }
            int moveChoice = scanner.nextInt() - 1;
            Move chosenMove = moves.get(moveChoice);

            System.out.println("Choose a target:");
            for (int i = 0; i < enemies.size(); i++) {
                System.out.println((i + 1) + ". " + enemies.get(i).getName());
            }
            int targetChoice = scanner.nextInt() - 1;
            Character chosenTarget = enemies.get(targetChoice);

            List<Character> targets = new ArrayList<>();
            targets.add(chosenTarget);
            return new ActionChoice(chosenMove, targets);
        };

        // AI-controlled: random move, random target.
        MoveSelector randomSelector = (actor, enemies) -> {
            List<Move> moves = actor.getMoves();
            Move chosenMove = moves.get(random.nextInt(moves.size()));
            Character chosenTarget = enemies.get(random.nextInt(enemies.size()));

            List<Character> targets = new ArrayList<>();
            targets.add(chosenTarget);
            return new ActionChoice(chosenMove, targets);
        };

        // --- Wire up the engine ---
        TypeChart typeChart = new TypeChart();
        RandomProvider randomProvider = new RandomProvider();
        DamageCalculator damageCalculator = new DamageCalculator(typeChart, randomProvider);
        TurnResolver turnResolver = new TurnResolver(damageCalculator, randomProvider);
        StatusEffectResolver statusEffectResolver = new StatusEffectResolver();

        List<Character> allCombatants = new ArrayList<>();
        allCombatants.addAll(playerTeam.getMembers());
        allCombatants.addAll(enemyTeam.getMembers());
        TurnOrderScheduler scheduler = new TurnOrderScheduler(allCombatants, randomProvider);

        Battle battle = new Battle(
                playerTeam, enemyTeam,
                consoleSelector, randomSelector,
                turnResolver, statusEffectResolver, scheduler
        );

        battle.run();

        scanner.close();
    }
}