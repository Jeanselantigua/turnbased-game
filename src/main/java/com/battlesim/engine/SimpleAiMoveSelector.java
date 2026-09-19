package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight AI for balance sims: recover when angered, finish Blessed
 * targets, heal an injured ally when possible, otherwise a random damaging
 * move aimed at the lowest-HP legal target.
 */
public class SimpleAiMoveSelector implements MoveSelector {

    private static final double HEAL_THRESHOLD = 0.40;
    private static final double RITUAL_ALLY_SAFE_HP = 0.50;
    private static final String RECOVER_MOVE = "Recover";
    private static final String ENLIGHTENMENT_MOVE = "Perfect Enlightenment";
    private static final int BLESSED_CAP = 5;

    private final RandomProvider random;

    public SimpleAiMoveSelector(RandomProvider random) {
        this.random = random;
    }

    @Override
    public ActionChoice chooseAction(Character actor, List<Move> availableMoves,
                                      List<Character> enemyTeam, List<Character> allyTeam) {
        List<Move> moves = availableMoves.isEmpty() ? actor.getMoves() : availableMoves;

        Move recover = findMove(moves, RECOVER_MOVE);
        if (recover != null) {
            return new ActionChoice(recover, List.of(actor));
        }

        Move enlightenment = findMove(moves, ENLIGHTENMENT_MOVE);
        if (enlightenment != null) {
            Character capped = firstAtBlessedCap(enemyTeam);
            if (capped != null) {
                return new ActionChoice(enlightenment, List.of(capped));
            }
            if (!ritualLooksRisky(actor, allyTeam)) {
                return new ActionChoice(enlightenment, List.of(lowestHp(enemyTeam)));
            }
        }

        Move heal = findHealMove(moves);
        Character injuredAlly = lowestHpBelow(allyTeam, HEAL_THRESHOLD);
        if (heal != null && injuredAlly != null) {
            return new ActionChoice(heal, List.of(injuredAlly));
        }

        List<Move> attacks = nonHealMoves(moves);
        Move chosen;
        if (!attacks.isEmpty()) {
            chosen = attacks.get(random.nextInt(0, attacks.size() - 1));
        } else {
            chosen = moves.get(0);
        }

        List<Character> pool = chosen.targetsAllies() ? allyTeam : enemyTeam;
        return new ActionChoice(chosen, List.of(lowestHp(pool)));
    }

    private static boolean ritualLooksRisky(Character actor, List<Character> allyTeam) {
        for (Character ally : allyTeam) {
            if (ally == actor || ally.isFainted()) {
                continue;
            }
            if (ally.getStats().getCurrentHp() < RITUAL_ALLY_SAFE_HP * ally.getStats().getMaxHp()) {
                return true;
            }
        }
        return false;
    }

    private static Character firstAtBlessedCap(List<Character> enemies) {
        for (Character enemy : enemies) {
            if (!enemy.isFainted()
                    && enemy.getBlessedStacks() >= BLESSED_CAP) {
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
            if (move.targetsAllies()) {
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
