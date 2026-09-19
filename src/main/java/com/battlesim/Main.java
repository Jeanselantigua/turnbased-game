package com.battlesim;

import com.battlesim.content.PlayableCharacters;
import com.battlesim.engine.ActionChoice;
import com.battlesim.engine.Battle;
import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.MoveSelector;
import com.battlesim.engine.RandomMoveSelector;
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
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Character definitions now live in content.PlayableCharacters —
        // Main just spawns fresh battle instances from those templates.
        Character knight = PlayableCharacters.knight().createInstance();
        Character rogue = PlayableCharacters.rogue().createInstance();
        Character monk = PlayableCharacters.monk().createInstance();
        Character caveman = PlayableCharacters.caveman().createInstance();
        Character chefromancer = PlayableCharacters.chefromancer().createInstance();
        Character healer = PlayableCharacters.healer().createInstance();

        Team playerTeam = new Team(List.of(chefromancer, knight, healer));
        Team enemyTeam = new Team(List.of(caveman, monk, rogue));

        System.out.println("You control Team A. Team B picks a random move and a random target each turn.");

        // Player-controlled: choose a move from console, then a target from console.
        MoveSelector consoleSelector = (actor, availableMoves, enemies, allies) -> {
            System.out.println(actor.getName() + "'s moves:");
            List<Move> moves = availableMoves.isEmpty() ? actor.getMoves() : availableMoves;
            for (int i = 0; i < moves.size(); i++) {
                System.out.println((i + 1) + ". " + moves.get(i).getName());
            }
            int moveChoice = scanner.nextInt() - 1;
            Move chosenMove = moves.get(moveChoice);

            List<Character> pool = chosenMove.legalTargets(actor, allies, enemies);
            Character chosenTarget;
            if (chosenMove.targetsSelfOnly()) {
                chosenTarget = actor;
            } else {
                System.out.println(chosenMove.targetsAllies() ? "Choose an ally:" : "Choose a target:");
                for (int i = 0; i < pool.size(); i++) {
                    Character candidate = pool.get(i);
                    System.out.println((i + 1) + ". " + candidate.getName()
                            + " (" + candidate.getStats().getCurrentHp()
                            + "/" + candidate.getStats().getMaxHp() + ")");
                }
                int targetChoice = scanner.nextInt() - 1;
                chosenTarget = pool.get(targetChoice);
            }

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
        MoveSelector randomSelector = new RandomMoveSelector(randomProvider);

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