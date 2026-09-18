package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight AI for balance sims: heal an injured ally when possible,
 * otherwise a random damaging move aimed at the lowest-HP legal target.
 */
public class SimpleAiMoveSelector implements MoveSelector {

    private static final double HEAL_THRESHOLD = 0.40;

    private final RandomProvider random;

    public SimpleAiMoveSelector(RandomProvider random) {
        this.random = random;
    }

    @Override
    public ActionChoice chooseAction(Character actor, List<Character> enemyTeam, List<Character> allyTeam) {
        Move heal = findHealMove(actor);
        Character injuredAlly = lowestHpBelow(allyTeam, HEAL_THRESHOLD);
        if (heal != null && injuredAlly != null) {
            return new ActionChoice(heal, List.of(injuredAlly));
        }

        List<Move> attacks = nonHealMoves(actor);
        Move chosen;
        if (!attacks.isEmpty()) {
            chosen = attacks.get(random.nextInt(0, attacks.size() - 1));
        } else {
            chosen = actor.getMoves().get(0);
        }

        List<Character> pool = chosen.targetsAllies() ? allyTeam : enemyTeam;
        return new ActionChoice(chosen, List.of(lowestHp(pool)));
    }

    private static Move findHealMove(Character actor) {
        for (Move move : actor.getMoves()) {
            if (move.targetsAllies()) {
                return move;
            }
        }
        return null;
    }

    private static List<Move> nonHealMoves(Character actor) {
        List<Move> attacks = new ArrayList<>();
        for (Move move : actor.getMoves()) {
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
