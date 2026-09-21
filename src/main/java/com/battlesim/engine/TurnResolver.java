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

        resolveHits(actor, move, choice.getTargets(), context, log);

        if (!move.targetsAllies() && !actor.isFainted()) {
            boolean repeat = false;
            for (Passive passive : snapshotPassives(actor)) {
                if (passive.shouldRepeatAction(actor, move, choice.getTargets(), context, log)) {
                    repeat = true;
                }
            }
            if (repeat) {
                resolveHits(actor, move, choice.getTargets(), context, log);
            }
        }

        for (Passive passive : snapshotPassives(actor)) {
            passive.onActionResolved(actor, move, choice.getTargets(), context, log);
        }

        return log;
    }

    private void resolveHits(Character actor, Move move, List<Character> targets,
                              BattleContext context, List<String> log) {
        for (Character target : targets) {
            if (actor.isFainted() || target.isFainted()) {
                continue;
            }
            resolveHitOnTarget(actor, move, target, context, log);
        }
    }

    private void resolveHitOnTarget(Character actor, Move move, Character target,
                                     BattleContext context, List<String> log) {
        int attackerAccuracy = move.getAccuracy();
        for (Passive passive : snapshotPassives(actor)) {
            attackerAccuracy = passive.modifyAccuracy(actor, target, move, attackerAccuracy);
        }
        int effectiveAccuracy = attackerAccuracy;
        if (!move.targetsAllies()) {
            for (Passive passive : snapshotPassives(target)) {
                effectiveAccuracy = passive.modifyIncomingAccuracy(target, actor, move, effectiveAccuracy);
            }
        }
        effectiveAccuracy = Math.max(0, Math.min(100, effectiveAccuracy));

        boolean hits = randomProvider.nextInt(1, 100) <= effectiveAccuracy;
        boolean dodged = false;
        if (hits && !move.targetsAllies()) {
            for (Passive passive : snapshotPassives(target)) {
                if (passive.rollDodge(target, actor, move)) {
                    dodged = true;
                    break;
                }
            }
        }
        if (!hits || dodged) {
            if (dodged || effectiveAccuracy < attackerAccuracy) {
                log.add(target.getName() + " dodges the attack!");
            } else {
                log.add("Missed " + target.getName() + "!");
            }
            if (dodged) {
                for (Passive passive : snapshotPassives(target)) {
                    passive.onDodged(target, actor, move, context, log);
                }
            } else {
                for (Passive passive : snapshotPassives(actor)) {
                    passive.onAttackMissed(actor, move, log);
                }
            }
            return;
        }

        if (move.targetsAllies()) {
            resolveAllyMove(actor, move, target, log);
            return;
        }

        boolean isCrit = false;
        for (Passive passive : snapshotPassives(actor)) {
            if (passive.rollBonusCrit(actor, move)) {
                isCrit = true;
            }
        }

        double damage = damageCalculator.calculateDamage(actor, target, move);
        for (Passive passive : snapshotPassives(actor)) {
            damage = passive.modifyOutgoingDamage(actor, target, move, damage, isCrit, log);
        }
        double damageBeforeIncoming = damage;
        for (Passive passive : snapshotPassives(target)) {
            damage = passive.modifyIncomingDamage(target, actor, move, damage, log);
        }
        if (context != null) {
            for (Character ally : context.alliesOf(target)) {
                for (Passive passive : snapshotPassives(ally)) {
                    damage = passive.modifyIncomingDamageToAlly(ally, target, actor, move, damage, log);
                }
            }
        }

        boolean blocked = damageBeforeIncoming > 0 && damage <= 0;
        int finalDamage = (int) Math.round(Math.max(0, damage));
        if (finalDamage > 0) {
            int actualDamage = statusEffectResolver.applyDamageThroughShield(
                    target, actor, finalDamage, context, log);
            if (actualDamage > 0) {
                log.add(target.getName() + " took " + actualDamage + " damage!" + (isCrit ? " Critical hit!" : ""));
                for (Passive passive : snapshotPassives(target)) {
                    passive.onDamageTaken(target, actor, actualDamage, log);
                }

                tryApplyStatus(move, actor, target, log);

                for (Passive passive : snapshotPassives(actor)) {
                    passive.onHitLanded(actor, target, move, actualDamage, isCrit, log, context);
                }

                statusEffectResolver.applyLeechOnDamage(actor, move, actualDamage, log);

                if (target.isFainted()) {
                    log.add(target.getName() + " has fainted!");
                    statusEffectResolver.notifyFaint(target, actor, move, context, log);
                }
            }
        }

        for (Passive passive : snapshotPassives(actor)) {
            passive.onAttackConnected(actor, target, move, finalDamage, isCrit, blocked, log, context);
        }
    }

    /** Copy so a hook can add/remove passives without crashing the iterator. */
    private static List<Passive> snapshotPassives(Character character) {
        return new ArrayList<>(character.getPassives());
    }

    private boolean canAct(Character actor, List<String> log) {
        if (actor.consumeQueuedSkip()) {
            log.add(actor.getName() + " is paralyzed and can't move!");
            return false;
        }
        if (actor.getStatus() == Status.STUN) {
            log.add(actor.getName() + " is stunned and can't move!");
            actor.setStatus(Status.NONE);
            return false;
        }
        if (actor.getStatus() == Status.PARALYSIS && randomProvider.nextDouble() < 0.25) {
            log.add(actor.getName() + " is paralyzed and can't move!");
            int skipTurns = Math.max(1, actor.getStatusMagnitude());
            if (skipTurns > 1) {
                actor.queueSkipTurns(skipTurns - 1);
            }
            return false;
        }
        return true;
    }

    private void resolveAllyMove(Character actor, Move move, Character target, List<String> log) {
        if (move.getInflictedStatus() == Status.HEAL) {
            int before = target.getStats().getCurrentHp();
            statusEffectResolver.applyHeal(target, log);
            int healed = target.getStats().getCurrentHp() - before;
            for (Passive passive : snapshotPassives(actor)) {
                passive.onAllyHealed(actor, target, healed, log);
            }
        }
        Status status = move.getInflictedStatus();
        if (status == Status.SHIELD || status == Status.SELF_SHIELD) {
            Character shielded = status == Status.SELF_SHIELD ? actor : target;
            statusEffectResolver.applyShield(shielded, move.getPower(), log);
            if (status == Status.SHIELD) {
                for (Passive passive : snapshotPassives(actor)) {
                    passive.onAllyShielded(actor, shielded, move.getPower(), log);
                }
            }
        }
    }

    private void tryApplyStatus(Move move, Character actor, Character target, List<String> log) {
        Status status = move.getInflictedStatus();
        if (status == Status.NONE || status == Status.HEAL || status == Status.SHIELD
                || status == Status.SELF_SHIELD || status == Status.LEECH) {
            return;
        }
        if (status == Status.SIPHON) {
            if (rollStatusChance(actor, target, move, log)) {
                target.applySiphon(actor, status.getDefaultDurationTurns());
                log.add(target.getName() + " is being siphoned!");
            }
            return;
        }
        if (target.getStatus() != Status.NONE) {
            return;
        }
        if (rollStatusChance(actor, target, move, log)) {
            Status applied = move.getInflictedStatus();
            for (Passive passive : snapshotPassives(target)) {
                if (passive.isImmuneTo(applied)) {
                    log.add(target.getName() + " resists " + applied + "!");
                    return;
                }
            }
            int magnitude = applied == Status.BLEED ? actor.getStats().getAttack() : 0;
            for (Passive passive : snapshotPassives(actor)) {
                magnitude = passive.modifyOutgoingStatusMagnitude(actor, target, applied, magnitude, log);
            }
            target.setStatus(applied, magnitude);
            log.add(target.getName() + " is now " + applied + "!");
            for (Passive passive : snapshotPassives(target)) {
                passive.onStatusReceived(target, applied, actor, log);
            }
        }
    }

    private boolean rollStatusChance(Character actor, Character target, Move move, List<String> log) {
        if (randomProvider.nextInt(1, 100) <= move.getStatusChance()) {
            return true;
        }
        for (Passive passive : snapshotPassives(actor)) {
            if (passive.shouldRerollFailedStatus(actor, target, move, log)) {
                return randomProvider.nextInt(1, 100) <= move.getStatusChance();
            }
        }
        return false;
    }
}
