package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;
import com.battlesim.model.Team;
import java.util.ArrayList;
import java.util.List;

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
                break;
            }

            boolean actorOnTeamA = teamA.getMembers().contains(actor);
            List<Character> enemies = actorOnTeamA ? teamB.getMembers() : teamA.getMembers();
            MoveSelector selector = actorOnTeamA ? selectorA : selectorB;

            List<Character> livingEnemies = targetableOnly(enemies);
            if (livingEnemies.isEmpty()) {
                List<Character> anyoneAlive = aliveOnly(enemies);
                if (anyoneAlive.isEmpty()) {
                    break;
                }
                livingEnemies = anyoneAlive;
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

    private List<Character> targetableOnly(List<Character> characters) {
        List<Character> targetable = new ArrayList<>();
        for (Character character : aliveOnly(characters)) {
            boolean hidden = false;
            for (Passive passive : character.getPassives()) {
                if (passive.isUntargetable(character)) {
                    hidden = true;
                    break;
                }
            }
            if (!hidden) {
                targetable.add(character);
            }
        }
        return targetable;
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