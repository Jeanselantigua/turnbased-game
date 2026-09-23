package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.MoveKit;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.progress.Growth;
import java.util.ArrayList;
import java.util.List;

/**
 * Ult: copy an enemy's best attack and perform it perfectly (never misses,
 * effects always land, rettyped to Holy for STAB, source-kit on-hit effects
 * still fire). A copied ult can be used once; a copied basic can be used
 * three times. Then this ult goes on cooldown.
 */
public class MonkMasterOfAnyArtPassive implements Passive {

    public static final String MOVE_NAME = "Master of Any Art";
    public static final int ULT_USES = 1;
    public static final int BASIC_USES = 3;

    private Character source;
    private Move copiedArt;
    private int usesRemaining;
    private boolean performingCopy;

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.HOLY, 0, 100, 0, true, Status.NONE, 0,
                Growth.ULT_COOLDOWN_TURNS);
    }

    public Move getCopiedArt() {
        return copiedArt;
    }

    public int getUsesRemaining() {
        return usesRemaining;
    }

    public static Move bestMoveToCopy(Character source) {
        if (source == null || source.getMoves().isEmpty()) {
            return null;
        }
        MoveKit kit = source.getKit();
        if (kit != null) {
            Move ult = kit.getUlt();
            if (ult != null && source.knowsMove(ult.getName())) {
                return ult;
            }
        }
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move move : source.getMoves()) {
            if (move.targetsAllies() || move.targetsSelfOnly()) {
                continue;
            }
            int score = Math.max(1, move.getPower()) * move.getMaxHits();
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best != null ? best : source.getMoves().get(0);
    }

    public static Move perfectCopy(Move original) {
        if (original == null) {
            return null;
        }
        Status status = original.getInflictedStatus();
        int statusChance = status == Status.NONE ? 0 : 100;
        return new Move(original.getName(), Type.HOLY, original.getPower(), 100,
                original.getPriority(), original.isMagic(), status, statusChance, 0,
                original.getMinHits(), original.getMaxHits());
    }

    @Override
    public Move rewriteMove(Character self, Move move, List<Character> targets,
                             BattleContext context, List<String> log) {
        if (move == null || !MOVE_NAME.equals(move.getName())) {
            return move;
        }
        if (copiedArt == null) {
            Character picked = firstLiving(targets);
            Move art = bestMoveToCopy(picked);
            if (art == null) {
                log.add(self.getName() + " finds no art to master.");
                return move;
            }
            source = picked;
            copiedArt = art;
            usesRemaining = isUlt(picked, art) ? ULT_USES : BASIC_USES;
            log.add(self.getName() + " masters " + art.getName()
                    + " (" + usesRemaining + (usesRemaining == 1 ? " use" : " uses") + ")!");
        }
        usesRemaining--;
        performingCopy = true;
        return perfectCopy(copiedArt);
    }

    @Override
    public boolean attackCannotBeDodged(Character self, Move move) {
        return performingCopy;
    }

    @Override
    public int modifyAccuracy(Character self, Character target, Move move, int baseAccuracy) {
        return performingCopy ? 100 : baseAccuracy;
    }

    @Override
    public List<Move> followUpHits(Character self, Move move, List<Character> targets,
                                    BattleContext context, List<String> log) {
        if (!performingCopy || source == null) {
            return List.of();
        }
        Move probe = copiedArt != null ? copiedArt : move;
        List<Move> extras = new ArrayList<>();
        for (Passive passive : source.getPassives()) {
            for (Move extra : passive.followUpHits(self, probe, targets, context, log)) {
                extras.add(perfectCopy(extra));
            }
        }
        return extras;
    }

    @Override
    public boolean shouldRepeatAction(Character self, Move move, List<Character> targets,
                                       BattleContext context, List<String> log) {
        if (!performingCopy || source == null) {
            return false;
        }
        for (Passive passive : source.getPassives()) {
            if (passive.shouldRepeatAction(self, move, targets, context, log)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onHitLanded(Character self, Character target, Move move,
                             int damageDealt, boolean isCrit, List<String> log,
                             BattleContext context) {
        if (!performingCopy || source == null) {
            return;
        }
        for (Passive passive : source.getPassives()) {
            passive.onHitLanded(self, target, move, damageDealt, isCrit, log, context);
        }
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        if (!performingCopy || source == null) {
            return;
        }
        for (Passive passive : source.getPassives()) {
            passive.onAttackConnected(self, target, move, damageDealt, isCrit, blocked, log, context);
        }
    }

    @Override
    public int modifyOutgoingStatusMagnitude(Character self, Character target, Status status,
                                              int magnitude, List<String> log) {
        if (!performingCopy || source == null) {
            return magnitude;
        }
        int value = magnitude;
        for (Passive passive : source.getPassives()) {
            value = passive.modifyOutgoingStatusMagnitude(self, target, status, value, log);
        }
        return value;
    }

    @Override
    public void onActionResolved(Character self, Move move, List<Character> targets,
                                  BattleContext context, List<String> log) {
        if (!performingCopy) {
            return;
        }
        performingCopy = false;
        if (usesRemaining > 0) {
            self.clearMoveCooldown(MOVE_NAME);
            return;
        }
        copiedArt = null;
        source = null;
        self.startMoveCooldown(MOVE_NAME, Growth.ULT_COOLDOWN_TURNS);
    }

    private static boolean isUlt(Character owner, Move move) {
        if (owner == null || move == null || owner.getKit() == null) {
            return false;
        }
        Move ult = owner.getKit().getUlt();
        return ult != null && ult.getName().equals(move.getName());
    }

    private static Character firstLiving(List<Character> targets) {
        if (targets == null) {
            return null;
        }
        for (Character target : targets) {
            if (target != null && !target.isFainted()) {
                return target;
            }
        }
        return null;
    }
}
