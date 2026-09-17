package com.battlesim.model;

import java.util.List;

/**
 * A group of Characters fighting on the same side. Unlike a classic
 * "one active member" system, all living members of a Team are eligible
 * to act — the TurnOrderScheduler decides who, individually, goes next
 * based on speed.
 */
public class Team {

    private final List<Character> members;

    public Team(List<Character> members) {
        if (members.isEmpty()) {
            throw new IllegalArgumentException("A team needs at least one Character");
        }
        this.members = members;
    }

    public List<Character> getMembers() {
        return members;
    }

    public boolean hasAnyAlive() {
        return members.stream().anyMatch(character -> !character.isFainted());
    }
}
