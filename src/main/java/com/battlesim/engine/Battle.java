package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Team;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a full battle between two Teams. The TurnOrderScheduler (built by
 * the caller from every living combatant on both sides) decides who acts
 * next; that Character's MoveSelector picks a Move + targets; TurnResolver
 * executes it; StatusEffectResolver ticks damage-over-time; repeat until
 * one team is wiped.
 */
public class Battle {

    private final Team teamA;
    private final Team teamB;
    private final MoveSelector selectorA;
    private final MoveSelector selectorB;
    private final TurnResolver turnResolver;
    private final StatusEffectResolver statusEffectResolver;
    private final TurnOrderScheduler scheduler;

    public Battle(Team teamA, Team teamB,
                   MoveSelector selectorA, MoveSelector selectorB,
                   TurnResolver turnResolver,
                   StatusEffectResolver statusEffectResolver,
                   TurnOrderScheduler scheduler) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.selectorA = selectorA;
        this.selectorB = selectorB;
        this.turnResolver = turnResolver;
        this.statusEffectResolver = statusEffectResolver;
        this.scheduler = scheduler;
    }

    public void run() {
        while (teamA.hasAnyAlive() && teamB.hasAnyAlive()) {
            Character actor = scheduler.getNextActor();
            if (actor == null) {
                break; // no one left able to act — shouldn't normally happen
            }

            boolean actorOnTeamA = teamA.getMembers().contains(actor);
            List<Character> enemies = actorOnTeamA ? teamB.getMembers() : teamA.getMembers();
            MoveSelector selector = actorOnTeamA ? selectorA : selectorB;

            List<Character> livingEnemies = aliveOnly(enemies);
            if (livingEnemies.isEmpty()) {
                break; // opposing team just got wiped mid-loop
            }

            ActionChoice choice = selector.chooseAction(actor, livingEnemies);
            List<String> log = turnResolver.resolveAction(actor, choice);
            statusEffectResolver.applyEndOfTurnEffects(actor, log);
            log.forEach(System.out::println);

            scheduler.advanceActor(actor);
        }

        announceResult();
    }

    private List<Character> aliveOnly(List<Character> characters) {
        List<Character> alive = new ArrayList<>();
        for (Character character : characters) {
            if (!character.isFainted()) {
                alive.add(character);
            }
        }
        return alive;
    }

    private void announceResult() {
        if (teamA.hasAnyAlive()) {
            System.out.println("Team A wins!");
        } else if (teamB.hasAnyAlive()) {
            System.out.println("Team B wins!");
        } else {
            System.out.println("It's a draw — both teams were wiped!");
        }
    }
}
