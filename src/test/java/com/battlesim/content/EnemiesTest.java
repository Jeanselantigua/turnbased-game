package com.battlesim.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.EnemyTemplate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class EnemiesTest {

    @Test
    public void allReturnsOneOfEachRankWithUniqueNames() {
        List<EnemyTemplate> roster = Enemies.all();
        Set<String> names = roster.stream()
                .map(EnemyTemplate::getName)
                .collect(Collectors.toSet());
        assertEquals(roster.size(), names.size());
        assertTrue(roster.stream().anyMatch(e -> e.getRank() == EnemyRank.NORMAL));
        assertTrue(roster.stream().anyMatch(e -> e.getRank() == EnemyRank.ELITE));
        assertTrue(roster.stream().anyMatch(e -> e.getRank() == EnemyRank.BOSS));
    }
}
