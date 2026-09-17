package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves a single Character's action: checks whether they can act
 * (status conditions), then applies their chosen Move against each
 * chosen target — accuracy roll, damage, status application.
 */
public class TurnResolver {

    private final DamageCalculator damageCalculator;
    private final RandomProvider randomProvider;

    public TurnResolver(DamageCalculator damageCalculator, RandomProvider randomProvider) {
        this.damageCalculator = damageCalculator;
        this.randomProvider = randomProvider;
    }

    public List<String> resolveAction(Character actor, ActionChoice choice) {
        List<String> log = new ArrayList<>();

        if (!canAct(actor, log)) {
            return log;
        }

        Move move = choice.getMove();
        log.add(actor.getName() + " uses " + move.getName() + "!");

        for (Character target : choice.getTargets()) {
            resolveHitOnTarget(actor, move, target, log);
        }

        return log;
    }

    private void resolveHitOnTarget(Character actor, Move move, Character target, List<String> log) {
        boolean hits = randomProvider.nextInt(1, 100) <= move.getAccuracy();
        if (!hits) {
            log.add("Missed " + target.getName() + "!");
            return;
        }

        int damage = damageCalculator.calculateDamage(actor, target, move);
        if (damage > 0) {
            int actualDamage = target.getStats().applyDamage(damage);
            log.add(target.getName() + " took " + actualDamage + " damage!");
            if (target.isFainted()) {
                log.add(target.getName() + " has fainted!");
            }
        }

        tryApplyStatus(move, target, log);
    }

    private boolean canAct(Character actor, List<String> log) {
        if (actor.getStatus() == Status.STUN) {
            log.add(actor.getName() + " is stunned and can't move!");
            actor.setStatus(Status.NONE);
            return false;
        }
        if (actor.getStatus() == Status.PARALYSIS && randomProvider.nextDouble() < 0.25) {
            log.add(actor.getName() + " is paralyzed and can't move!");
            return false;
        }
        return true;
    }

    private void tryApplyStatus(Move move, Character target, List<String> log) {
        if (move.getInflictedStatus() == Status.NONE || target.getStatus() != Status.NONE) {
            return;
        }
        if (randomProvider.nextInt(1, 100) <= move.getStatusChance()) {
            target.setStatus(move.getInflictedStatus());
            log.add(target.getName() + " is now " + move.getInflictedStatus() + "!");
        }
    }
}
