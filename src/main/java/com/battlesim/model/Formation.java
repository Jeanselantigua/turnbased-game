package com.battlesim.model;

import java.util.ArrayList;
import java.util.List;

/** Neighbors in a team's current slot order (left/right of a fighter). */
public final class Formation {

    private Formation() {
    }

    public static List<Character> adjacentLiving(Character target, List<Character> team) {
        List<Character> adjacent = new ArrayList<>();
        if (target == null || team == null) {
            return adjacent;
        }
        int index = team.indexOf(target);
        if (index < 0) {
            return adjacent;
        }
        addIfLiving(adjacent, team, index - 1, target);
        addIfLiving(adjacent, team, index + 1, target);
        return adjacent;
    }

    private static void addIfLiving(List<Character> into, List<Character> team, int index,
                                     Character skip) {
        if (index < 0 || index >= team.size()) {
            return;
        }
        Character neighbor = team.get(index);
        if (neighbor != skip && neighbor != null && !neighbor.isFainted()) {
            into.add(neighbor);
        }
    }
}
