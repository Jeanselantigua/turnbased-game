package com.battlesim.content.passives;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Formation;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Chain Lightning jumps to an adjacent living enemy (up to 5 extra hits).
 * Thunder God's Wrath is a full-field AOE via {@link Move#hitsAllEnemies()}.
 */
public class WizardStormPassive implements Passive {

    public static final String CHAIN_NAME = "Chain Lightning";
    public static final String WRATH_NAME = "Thunder God's Wrath";
    public static final int MAX_CHAINS = 5;
    public static final int CHAIN_CHANCE_PERCENT = 70;

    private final RandomProvider random;

    public WizardStormPassive() {
        this(new RandomProvider());
    }

    public WizardStormPassive(RandomProvider random) {
        this.random = random != null ? random : new RandomProvider();
    }

    @Override
    public List<Character> expandTargets(Character self, Move move, List<Character> chosen,
                                          BattleContext context, List<String> log) {
        if (move == null || !CHAIN_NAME.equals(move.getName()) || context == null) {
            return chosen;
        }
        if (chosen == null || chosen.isEmpty()) {
            return chosen;
        }
        List<Character> enemies = context.enemiesOf(self);
        List<Character> hits = new ArrayList<>();
        Set<Character> seen = new LinkedHashSet<>();
        Character current = chosen.get(0);
        hits.add(current);
        seen.add(current);
        for (int bounce = 0; bounce < MAX_CHAINS; bounce++) {
            if (random.nextInt(1, 100) > CHAIN_CHANCE_PERCENT) {
                break;
            }
            List<Character> neighbors = new ArrayList<>();
            for (Character neighbor : Formation.adjacentLiving(current, enemies)) {
                if (!seen.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            if (neighbors.isEmpty()) {
                break;
            }
            current = neighbors.get(random.nextInt(0, neighbors.size() - 1));
            hits.add(current);
            seen.add(current);
            log.add("Chain Lightning arcs to " + current.getName() + "!");
        }
        return hits;
    }
}
