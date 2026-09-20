package com.battlesim.dungeon;

import com.battlesim.model.Character;
import com.battlesim.model.EnemyTemplate;
import com.battlesim.model.Team;
import java.util.ArrayList;
import java.util.List;

/** One fight in a dungeon: a list of enemy templates spawned together. */
public class Wave {

    private final List<EnemyTemplate> enemies;

    public Wave(List<EnemyTemplate> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            throw new IllegalArgumentException("A wave needs at least one enemy");
        }
        this.enemies = List.copyOf(enemies);
    }

    public List<EnemyTemplate> getEnemies() {
        return enemies;
    }

    public Team spawn(double statMultiplier) {
        List<Character> members = new ArrayList<>();
        for (EnemyTemplate enemy : enemies) {
            members.add(enemy.createInstance(statMultiplier));
        }
        return new Team(members);
    }
}
