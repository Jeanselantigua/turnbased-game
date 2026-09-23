package com.battlesim.item;

import com.battlesim.dungeon.Wave;
import com.battlesim.model.EnemyRank;
import com.battlesim.model.EnemyTemplate;
import com.battlesim.model.Gear;
import com.battlesim.model.GearRarity;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Gold and gear from a cleared wave. Rank decides drop chance and rarity;
 * dungeon difficulty only scales the gold. Sims can ignore the bag.
 */
public final class DropTable {

    public static final int GOLD_NORMAL = 15;
    public static final int GOLD_ELITE = 40;
    public static final int GOLD_BOSS = 120;

    private DropTable() {
    }

    public static Loot roll(Wave wave, double difficultyScale, RandomProvider random) {
        if (wave == null || random == null) {
            return Loot.EMPTY;
        }
        int gold = 0;
        List<Gear> drops = new ArrayList<>();
        for (EnemyTemplate enemy : wave.getEnemies()) {
            gold += goldFor(enemy.getRank(), difficultyScale);
            Gear piece = maybeDrop(enemy.getRank(), random);
            if (piece != null) {
                drops.add(piece);
            }
        }
        return new Loot(gold, drops);
    }

    /**
     * Guaranteed waypoint chest: elite gold × 2 and one piece. Rarity uses the
     * elite table through floor 49, then the boss table.
     */
    public static Loot chest(int waveNumber, double difficultyScale, RandomProvider random) {
        if (random == null) {
            return Loot.EMPTY;
        }
        int gold = goldFor(EnemyRank.ELITE, difficultyScale) * 2;
        GearRarity rarity = waveNumber >= 50
                ? bossRarity(random.nextDouble())
                : eliteRarity(random.nextDouble());
        return new Loot(gold, List.of(GearFactory.random(rarity, random)));
    }

    public static int goldFor(EnemyRank rank, double difficultyScale) {
        if (rank == null || difficultyScale <= 0) {
            return 0;
        }
        int base = GOLD_NORMAL;
        if (rank == EnemyRank.ELITE) {
            base = GOLD_ELITE;
        } else if (rank == EnemyRank.BOSS) {
            base = GOLD_BOSS;
        }
        return Math.max(0, (int) Math.round(base * difficultyScale));
    }

    static Gear maybeDrop(EnemyRank rank, RandomProvider random) {
        if (rank == null || random == null) {
            return null;
        }
        if (rank == EnemyRank.BOSS) {
            return GearFactory.random(bossRarity(random.nextDouble()), random);
        }
        double roll = random.nextDouble();
        if (rank == EnemyRank.ELITE) {
            if (roll >= 0.75) {
                return null;
            }
            return GearFactory.random(eliteRarity(random.nextDouble()), random);
        }
        if (roll >= 0.20) {
            return null;
        }
        return GearFactory.random(normalRarity(random.nextDouble()), random);
    }

    private static GearRarity normalRarity(double roll) {
        return roll < 0.15 ? GearRarity.RARE : GearRarity.COMMON;
    }

    private static GearRarity eliteRarity(double roll) {
        if (roll < 0.15) {
            return GearRarity.LEGENDARY;
        }
        if (roll < 0.45) {
            return GearRarity.EPIC;
        }
        return GearRarity.RARE;
    }

    private static GearRarity bossRarity(double roll) {
        if (roll < 0.20) {
            return GearRarity.LEGENDARY;
        }
        if (roll < 0.60) {
            return GearRarity.EPIC;
        }
        return GearRarity.RARE;
    }

    public static final class Loot {
        public static final Loot EMPTY = new Loot(0, List.of());

        private final int gold;
        private final List<Gear> drops;

        public Loot(int gold, List<Gear> drops) {
            this.gold = Math.max(0, gold);
            this.drops = drops == null ? List.of() : List.copyOf(drops);
        }

        public int getGold() {
            return gold;
        }

        public List<Gear> getDrops() {
            return drops;
        }
    }
}
