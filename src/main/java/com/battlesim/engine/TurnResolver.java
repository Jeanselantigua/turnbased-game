package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

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
        int effectiveAccuracy = move.getAccuracy();
        for (Passive passive : actor.getPassives()) {
            effectiveAccuracy = passive.modifyAccuracy(actor, move, effectiveAccuracy);
        }

        boolean hits = randomProvider.nextInt(1, 100) <= effectiveAccuracy;
        if (!hits) {
            log.add("Missed " + target.getName() + "!");
            for (Passive passive : actor.getPassives()) {
                passive.onAttackMissed(actor, move, log);
            }
            return;
        }

        boolean isCrit = false;
        for (Passive passive : actor.getPassives()) {
            if (passive.rollBonusCrit(actor, move)) {
                isCrit = true;
            }
        }

        double damage = damageCalculator.calculateDamage(actor, target, move);
        for (Passive passive : actor.getPassives()) {
            damage = passive.modifyOutgoingDamage(actor, target, move, damage, isCrit, log);
        }
        for (Passive passive : target.getPassives()) {
            damage = passive.modifyIncomingDamage(target, actor, move, damage, log);
        }

        int finalDamage = (int) Math.round(Math.max(0, damage));
        if (finalDamage > 0) {
            int actualDamage = target.getStats().applyDamage(finalDamage);
            log.add(target.getName() + " took " + actualDamage + " damage!" + (isCrit ? " Critical hit!" : ""));
            for (Passive passive : target.getPassives()) {
                passive.onDamageTaken(target, actor, actualDamage, log);
            }
            
            tryApplyStatus(move, target, log);
            
            for (Passive passive : actor.getPassives()) {
                passive.onHitLanded(actor, target, move, finalDamage, isCrit, log);
            }
            
            if (target.isFainted()) {
                log.add(target.getName() + " has fainted!");
            }
        }
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