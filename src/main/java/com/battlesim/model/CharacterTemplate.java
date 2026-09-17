package com.battlesim.model;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CharacterTemplate {

    private final String name;
    private final int maxHp, attack, defense, magicAttack, magicDefense, speed;
    private final Type affinity;
    private final List<Move> moves;
    private final List<Supplier<Passive>> passiveSuppliers;

    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves,
                              List<Supplier<Passive>> passiveSuppliers) {
        this.name = name;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.magicAttack = magicAttack;
        this.magicDefense = magicDefense;
        this.speed = speed;
        this.affinity = affinity;
        this.moves = moves;
        this.passiveSuppliers = passiveSuppliers;
    }

    /** Convenience constructor for a template with no passives. */
    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves) {
        this(name, maxHp, attack, defense, magicAttack, magicDefense, speed,
                affinity, moves, List.of());
    }

    /** Spawns a fresh Character for battle — full HP, no status, new Passive instances. */
    public Character createInstance() {
        Stats stats = new Stats(maxHp, attack, defense, magicAttack, magicDefense, speed);
        List<Passive> passives = passiveSuppliers.stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
        return new Character(name, stats, affinity, List.copyOf(moves), passives);
    }

    public String getName() { return name; }
}