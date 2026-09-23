package com.battlesim.model;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CharacterTemplate {

    private final String name;
    private final int maxHp, attack, defense, magicAttack, magicDefense, speed;
    private final Type affinity;
    private final MoveKit kit;
    private final List<Supplier<Passive>> passiveSuppliers;
    private final List<StatKind> specialties;

    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves,
                              List<Supplier<Passive>> passiveSuppliers) {
        this(name, maxHp, attack, defense, magicAttack, magicDefense, speed,
                affinity, MoveKit.alwaysKnown(moves), passiveSuppliers, List.of());
    }

    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves,
                              List<Supplier<Passive>> passiveSuppliers,
                              List<StatKind> specialties) {
        this(name, maxHp, attack, defense, magicAttack, magicDefense, speed,
                affinity, MoveKit.alwaysKnown(moves), passiveSuppliers, specialties);
    }

    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, MoveKit kit,
                              List<Supplier<Passive>> passiveSuppliers,
                              List<StatKind> specialties) {
        this.name = name;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.magicAttack = magicAttack;
        this.magicDefense = magicDefense;
        this.speed = speed;
        this.affinity = affinity;
        this.kit = kit;
        this.passiveSuppliers = passiveSuppliers;
        this.specialties = List.copyOf(specialties);
    }

    /** Convenience constructor for a template with no passives. */
    public CharacterTemplate(String name, int maxHp, int attack, int defense,
                              int magicAttack, int magicDefense, int speed,
                              Type affinity, List<Move> moves) {
        this(name, maxHp, attack, defense, magicAttack, magicDefense, speed,
                affinity, moves, List.of());
    }

    /** Spawns a fresh Character — full HP, no status, only moves unlocked at level 1. */
    public Character createInstance() {
        return spawn(kit.unlockedAt(1));
    }

    /**
     * All kit moves known, still this template's stats. For combat/AI tests that
     * need Meditate, Roar, etc. without walking the XP ladder.
     */
    public Character createFullyLearnedInstance() {
        return spawn(kit.unlockedAt(Integer.MAX_VALUE));
    }

    /**
     * PvP / arena spawn: full kit, ult already ticking its normal cooldown.
     */
    public Character createPvpInstance() {
        Character fighter = createFullyLearnedInstance();
        fighter.putUltOnCooldown();
        return fighter;
    }

    /**
     * Same kit, passives, and specialties with different combat stats
     * (e.g. Phase 2 arena numbers on the current move ladder).
     */
    public CharacterTemplate withStats(int maxHp, int attack, int defense,
                                       int magicAttack, int magicDefense, int speed) {
        return new CharacterTemplate(name, maxHp, attack, defense, magicAttack,
                magicDefense, speed, affinity, kit, passiveSuppliers, specialties);
    }

    private Character spawn(List<Move> knownMoves) {
        Stats stats = new Stats(maxHp, attack, defense, magicAttack, magicDefense, speed);
        List<Passive> passives = passiveSuppliers.stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
        return new Character(name, stats, affinity, knownMoves, passives, specialties, kit);
    }

    public String getName() { return name; }
    public List<StatKind> getSpecialties() { return specialties; }
    public MoveKit getKit() { return kit; }
}
