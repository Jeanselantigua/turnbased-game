package com.battlesim.model;

import java.util.List;

public class CharacterTemplate {

    private final String name;
    private final int maxHp, attack, defense, magicAttack, magicDefense, speed;
    private final Type affinity;
    private final List<Move> moves;

    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves) {
        this.name = name;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.magicAttack = magicAttack;
        this.magicDefense = magicDefense;
        this.speed = speed;
        this.affinity = affinity;
        this.moves = moves;
    }

    /** Spawns a fresh Character for battle — full HP, no status. */
    public Character createInstance() {
        Stats stats = new Stats(maxHp, attack, defense, magicAttack, magicDefense, speed);
        return new Character(name, stats, affinity, List.copyOf(moves));
    }

    public String getName() { return name; }
}