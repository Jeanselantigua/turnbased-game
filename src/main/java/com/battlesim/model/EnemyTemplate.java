package com.battlesim.model;

import com.battlesim.content.passives.StatusImmunityPassive;
import com.battlesim.content.passives.ExtraActionsPassive;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * An enemy definition: a {@link CharacterTemplate} plus rank and boss extras.
 * Spawn with {@link #createInstance()} the same way playable characters do.
 */
public class EnemyTemplate {

    private final CharacterTemplate character;
    private final EnemyRank rank;
    private final Set<Status> immunities;
    private final int extraActionsPerTurn;

    public EnemyTemplate(CharacterTemplate character, EnemyRank rank) {
        this(character, rank, Set.of(), 0);
    }

    public EnemyTemplate(CharacterTemplate character, EnemyRank rank,
                         Set<Status> immunities, int extraActionsPerTurn) {
        this.character = character;
        this.rank = rank;
        this.immunities = immunities.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(immunities));
        this.extraActionsPerTurn = Math.max(0, extraActionsPerTurn);
    }

    public Character createInstance() {
        return createInstance(1.0);
    }

    /**
     * @param statMultiplier 1.0 = as written; dungeon waves pass a value &gt; 1 so later
     *                       floors get harder. Scaling is not wired yet.
     */
    public Character createInstance(double statMultiplier) {
        Character spawned = character.createInstance();
        if (!immunities.isEmpty()) {
            spawned.addPassive(new StatusImmunityPassive(immunities));
        }
        // TODO: if extraActionsPerTurn > 0, add an ExtraActionsPassive
        if (extraActionsPerTurn > 0) {
            spawned.addPassive(new ExtraActionsPassive(extraActionsPerTurn));
        }
        // TODO: if statMultiplier != 1.0, call spawned.getStats().scaleAll(statMultiplier)
        if (statMultiplier != 1.0) {
            spawned.getStats().scaleAll(statMultiplier);
        }
        return spawned;
    }

    public String getName() {
        return character.getName();
    }

    public EnemyRank getRank() {
        return rank;
    }

    public CharacterTemplate getCharacter() {
        return character;
    }

    public Set<Status> getImmunities() {
        return immunities;
    }

    public int getExtraActionsPerTurn() {
        return extraActionsPerTurn;
    }
}
