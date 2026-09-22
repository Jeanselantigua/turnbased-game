package com.battlesim.engine;

import com.battlesim.content.passives.HeavenPiercingBladePassive;
import com.battlesim.content.passives.MonkPerfectEnlightenmentPassive;
import com.battlesim.content.passives.RogueCritStealthPassive;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Status;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight AI for balance sims: recover when angered only if the foe is
 * already low, finish Blessed targets, start Meditate after
 * real 1v1 hits (or when allies are healthy), heal an injured ally when
 * possible, otherwise a random damaging move aimed at the lowest-HP legal target.
 */
public class SimpleAiMoveSelector implements MoveSelector {

    private static final double HEAL_THRESHOLD = 0.40;
    private static final double RITUAL_ALLY_SAFE_HP = 0.50;
    private static final double SOLO_CHANNEL_MIN_HP = 0.55;
    private static final double ANGERED_RECOVER_HP = 0.25;
    private static final String RECOVER_MOVE = MonkPerfectEnlightenmentPassive.RECOVER_NAME;
    private static final String ENLIGHTENMENT_MOVE = MonkPerfectEnlightenmentPassive.MOVE_NAME;
    private static final String SWING_MOVE = MonkPerfectEnlightenmentPassive.SWING_NAME;

    private final RandomProvider random;

    public SimpleAiMoveSelector(RandomProvider random) {
        this.random = random;
    }

    @Override
    public ActionChoice chooseAction(Character actor, List<Move> availableMoves,
                                      List<Character> enemyTeam, List<Character> allyTeam) {
        List<Move> moves = availableMoves;

        Move recover = findMove(moves, RECOVER_MOVE);
        Move swing = findMove(moves, SWING_MOVE);
        if (recover != null && (swing == null || shouldRecoverFromAnger(enemyTeam))) {
            return new ActionChoice(recover, List.of(actor));
        }

        Move hailMary = findMove(moves, HeavenPiercingBladePassive.MOVE_NAME);
        if (hailMary != null) {
            return new ActionChoice(hailMary, List.of(lowestHp(enemyTeam)));
        }

        Move ambush = findMove(moves, RogueCritStealthPassive.AMBUSH_NAME);
        if (ambush != null) {
            return new ActionChoice(ambush, List.of(lowestHp(enemyTeam)));
        }

        Move enlightenment = findMove(moves, ENLIGHTENMENT_MOVE);
        if (enlightenment != null) {
            Character capped = firstAtBlessedCap(enemyTeam);
            if (capped != null) {
                return new ActionChoice(enlightenment, List.of(capped));
            }
            if (shouldStartRitual(actor, allyTeam)) {
                return new ActionChoice(enlightenment, List.of(lowestHp(enemyTeam)));
            }
        }

        Move heal = findHealMove(moves);
        Character injuredAlly = lowestHpBelow(allyTeam, HEAL_THRESHOLD);
        if (heal != null && injuredAlly != null) {
            return new ActionChoice(heal, List.of(injuredAlly));
        }

        Move selfShield = findSelfShieldMove(moves);
        if (selfShield != null && !actor.getStats().hasShield()) {
            return new ActionChoice(selfShield, List.of(actor));
        }

        Move shield = findShieldMove(moves);
        Character unshieldedAlly = lowestHpWithoutShield(allyTeam);
        if (shield != null && unshieldedAlly != null) {
            return new ActionChoice(shield, List.of(unshieldedAlly));
        }

        List<Move> attacks = nonHealMoves(moves);
        attacks.removeIf(move -> ENLIGHTENMENT_MOVE.equals(move.getName()));
        Move chosen;
        if (!attacks.isEmpty()) {
            chosen = attacks.get(random.nextInt(0, attacks.size() - 1));
        } else {
            chosen = moves.get(0);
        }

        List<Character> pool = chosen.legalTargets(actor, allyTeam, enemyTeam);
        return new ActionChoice(chosen, List.of(lowestHp(pool)));
    }

    private static boolean shouldStartRitual(Character actor, List<Character> allyTeam) {
        if (ritualLooksRisky(actor, allyTeam)) {
            return false;
        }
        if (hasLivingAlly(actor, allyTeam)) {
            return true;
        }
        if (actor.getStats().getCurrentHp()
                < SOLO_CHANNEL_MIN_HP * actor.getStats().getMaxHp()) {
            return false;
        }
        MonkPerfectEnlightenmentPassive pe = actor.getPassive(MonkPerfectEnlightenmentPassive.class);
        return pe != null && pe.getIdleHitsLanded() >= MonkPerfectEnlightenmentPassive.SOLO_CHANNEL_MIN_HITS;
    }

    private static boolean ritualLooksRisky(Character actor, List<Character> allyTeam) {
        for (Character ally : allyTeam) {
            if (ally == actor || ally.isFainted() || ally.isSummon()) {
                continue;
            }
            if (ally.getStats().getCurrentHp() < RITUAL_ALLY_SAFE_HP * ally.getStats().getMaxHp()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLivingAlly(Character actor, List<Character> allyTeam) {
        for (Character ally : allyTeam) {
            if (ally != actor && !ally.isFainted() && !ally.isSummon()) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldRecoverFromAnger(List<Character> enemyTeam) {
        Character lowest = lowestHp(enemyTeam);
        if (lowest == null || lowest.isFainted()) {
            return true;
        }
        return lowest.getStats().getCurrentHp()
                <= ANGERED_RECOVER_HP * lowest.getStats().getMaxHp();
    }

    private static Character firstAtBlessedCap(List<Character> enemies) {
        for (Character enemy : enemies) {
            if (!enemy.isFainted()
                    && enemy.getBlessedStacks() >= MonkPerfectEnlightenmentPassive.BLESSED_CAP) {
                return enemy;
            }
        }
        return null;
    }

    private static Move findMove(List<Move> moves, String name) {
        for (Move move : moves) {
            if (name.equals(move.getName())) {
                return move;
            }
        }
        return null;
    }

    private static Move findHealMove(List<Move> moves) {
        for (Move move : moves) {
            if (move.getInflictedStatus() == Status.HEAL) {
                return move;
            }
        }
        return null;
    }

    private static Move findShieldMove(List<Move> moves) {
        for (Move move : moves) {
            if (move.getInflictedStatus() == Status.SHIELD) {
                return move;
            }
        }
        return null;
    }

    private static Move findSelfShieldMove(List<Move> moves) {
        for (Move move : moves) {
            if (move.getInflictedStatus() == Status.SELF_SHIELD) {
                return move;
            }
        }
        return null;
    }

    private static List<Move> nonHealMoves(List<Move> moves) {
        List<Move> attacks = new ArrayList<>();
        for (Move move : moves) {
            if (!move.targetsAllies()) {
                attacks.add(move);
            }
        }
        return attacks;
    }

    private static Character lowestHpBelow(List<Character> characters, double hpFraction) {
        Character lowest = null;
        for (Character character : characters) {
            if (character.getStats().getCurrentHp() >= hpFraction * character.getStats().getMaxHp()) {
                continue;
            }
            if (lowest == null || character.getStats().getCurrentHp() < lowest.getStats().getCurrentHp()) {
                lowest = character;
            }
        }
        return lowest;
    }

    private static Character lowestHpWithoutShield(List<Character> characters) {
        Character lowest = null;
        for (Character character : characters) {
            if (character.isFainted() || character.getStats().hasShield()) {
                continue;
            }
            if (lowest == null || character.getStats().getCurrentHp() < lowest.getStats().getCurrentHp()) {
                lowest = character;
            }
        }
        return lowest;
    }

    private static Character lowestHp(List<Character> characters) {
        Character lowest = characters.get(0);
        for (int i = 1; i < characters.size(); i++) {
            Character candidate = characters.get(i);
            if (candidate.getStats().getCurrentHp() < lowest.getStats().getCurrentHp()) {
                lowest = candidate;
            }
        }
        return lowest;
    }
}
