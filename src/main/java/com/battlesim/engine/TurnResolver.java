package com.battlesim.engine;

import com.battlesim.model.BattleContext;
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
    private final StatusEffectResolver statusEffectResolver;

    public TurnResolver(DamageCalculator damageCalculator, RandomProvider randomProvider) {
        this(damageCalculator, randomProvider, new StatusEffectResolver());
    }

    public TurnResolver(DamageCalculator damageCalculator, RandomProvider randomProvider,
                        StatusEffectResolver statusEffectResolver) {
        this.damageCalculator = damageCalculator;
        this.randomProvider = randomProvider;
        this.statusEffectResolver = statusEffectResolver;
    }

    public List<String> resolveAction(Character actor, ActionChoice choice) {
        return resolveAction(actor, choice, null);
    }

    public List<String> resolveAction(Character actor, ActionChoice choice, BattleContext context) {
        List<String> log = new ArrayList<>();

        if (!canAct(actor, log)) {
            return log;
        }

        Move move = choice.getMove();
        log.add(actor.getName() + " uses " + move.getName() + "!");

        for (Character target : choice.getTargets()) {
            resolveHitOnTarget(actor, move, target, context, log);
        }

        for (Passive passive : actor.getPassives()) {
            passive.onActionResolved(actor, move, choice.getTargets(), context, log);
        }

        return log;
    }

    private void resolveHitOnTarget(Character actor, Move move, Character target,
                                     BattleContext context, List<String> log) {
        int attackerAccuracy = move.getAccuracy();
        for (Passive passive : actor.getPassives()) {
            attackerAccuracy = passive.modifyAccuracy(actor, target, move, attackerAccuracy);
        }
        int effectiveAccuracy = attackerAccuracy;
        if (!move.targetsAllies()) {
            for (Passive passive : target.getPassives()) {
                effectiveAccuracy = passive.modifyIncomingAccuracy(target, actor, move, effectiveAccuracy);
            }
        }
        effectiveAccuracy = Math.max(0, Math.min(100, effectiveAccuracy));

        boolean hits = randomProvider.nextInt(1, 100) <= effectiveAccuracy;
        if (!hits) {
            if (effectiveAccuracy < attackerAccuracy) {
                log.add(target.getName() + " dodges the attack!");
            } else {
                log.add("Missed " + target.getName() + "!");
            }
            for (Passive passive : actor.getPassives()) {
                passive.onAttackMissed(actor, move, log);
            }
            return;
        }

        if (move.targetsAllies()) {
            resolveAllyMove(actor, move, target, log);
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
        if (context != null) {
            for (Character ally : context.alliesOf(target)) {
                for (Passive passive : ally.getPassives()) {
                    damage = passive.modifyIncomingDamageToAlly(ally, target, actor, move, damage, log);
                }
            }
        }

        int finalDamage = (int) Math.round(Math.max(0, damage));
        if (finalDamage > 0) {
            int actualDamage = target.getStats().applyDamage(finalDamage);
            log.add(target.getName() + " took " + actualDamage + " damage!" + (isCrit ? " Critical hit!" : ""));
            for (Passive passive : target.getPassives()) {
                passive.onDamageTaken(target, actor, actualDamage, log);
            }
            
            tryApplyStatus(move, actor, target, log);
            
            for (Passive passive : actor.getPassives()) {
                passive.onHitLanded(actor, target, move, finalDamage, isCrit, log, context);
            }

            statusEffectResolver.applyLeechOnDamage(actor, move, actualDamage, log);

            if (target.isFainted()) {
                log.add(target.getName() + " has fainted!");
                statusEffectResolver.applyOnFaintEffects(target, actor, log);
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

    private void resolveAllyMove(Character actor, Move move, Character target, List<String> log) {
        if (move.getInflictedStatus() == Status.HEAL) {
            int before = target.getStats().getCurrentHp();
            statusEffectResolver.applyHeal(target, log);
            int healed = target.getStats().getCurrentHp() - before;
            for (Passive passive : actor.getPassives()) {
                passive.onAllyHealed(actor, target, healed, log);
            }
        }
    }

    private void tryApplyStatus(Move move, Character actor, Character target, List<String> log) {
        Status status = move.getInflictedStatus();
        if (status == Status.NONE || status == Status.HEAL || status == Status.LEECH) {
            return;
        }
        if (status == Status.SIPHON) {
            if (randomProvider.nextInt(1, 100) <= move.getStatusChance()) {
                target.applySiphon(actor, status.getDefaultDurationTurns());
                log.add(target.getName() + " is being siphoned!");
            }
            return;
        }
        if (target.getStatus() != Status.NONE) {
            return;
        }
        if (randomProvider.nextInt(1, 100) <= move.getStatusChance()) {
            target.setStatus(move.getInflictedStatus());
            log.add(target.getName() + " is now " + move.getInflictedStatus() + "!");
        }
    }
}