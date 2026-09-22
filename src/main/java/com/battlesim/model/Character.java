package com.battlesim.model;

import com.battlesim.progress.Growth;
import com.battlesim.item.SetBonuses;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character {

    private final String name;
    private final Stats stats;
    private final Type affinity;
    private final List<Move> moves;
    private final List<Passive> passives;
    private final List<StatKind> specialties;
    private final MoveKit kit;
    private final Progression progression = new Progression();
    private final Loadout loadout = new Loadout();
    private final Map<String, Integer> moveCooldowns = new HashMap<>();
    private final Map<String, Integer> moveRanks = new HashMap<>();
    private final List<Move> lastUnlockedMoves = new ArrayList<>();
    private final Map<Status, StatusEffect> effects = new EnumMap<>(Status.class);
    /** Extra turns to skip after a multi-turn paralysis proc. */
    private int queuedSkipTurns;
    private Character siphonSource;
    private int siphonTurnsRemaining;
    private int woundStacks;
    private int woundTurnsRemaining;
    public static final int WOUND_STACK_CAP = 6;
    private Character summoner;
    private int blessedStacks;

    public Character(String name, Stats stats, Type affinity, List<Move> moves, List<Passive> passives) {
        this(name, stats, affinity, moves, passives, List.of());
    }

    public Character(String name, Stats stats, Type affinity, List<Move> moves,
                     List<Passive> passives, List<StatKind> specialties) {
        this(name, stats, affinity, moves, passives, specialties, MoveKit.alwaysKnown(moves));
    }

    public Character(String name, Stats stats, Type affinity, List<Move> moves,
                     List<Passive> passives, List<StatKind> specialties, MoveKit kit) {
        this.name = name;
        this.stats = stats;
        this.affinity = affinity;
        this.moves = new ArrayList<>(moves);
        this.passives = new ArrayList<>(passives);
        this.specialties = List.copyOf(specialties);
        this.kit = kit != null ? kit : MoveKit.alwaysKnown(moves);
        this.queuedSkipTurns = 0;
        this.siphonTurnsRemaining = 0;
    }

    /** Convenience constructor for a Character with no passives. */
    public Character(String name, Stats stats, Type affinity, List<Move> moves) {
        this(name, stats, affinity, moves, List.of());
    }

    public String getName() { return name; }
    public Stats getStats() { return stats; }
    public Type getAffinity() { return affinity; }
    public List<Move> getMoves() { return moves; }
    public List<Passive> getPassives() { return passives; }
    public List<StatKind> getSpecialties() { return specialties; }
    public Progression getProgression() { return progression; }
    public Loadout getLoadout() { return loadout; }
    public int getLevel() { return progression.getLevel(); }
    public int getXp() { return progression.getXp(); }
    public int getXpToNextLevel() { return progression.xpToNextLevel(); }
    public int getUnspentStatPoints() { return progression.getUnspentStatPoints(); }
    public int getUnspentMovePoints() { return progression.getUnspentMovePoints(); }

    /**
     * Awards XP and applies the automatic HP and speed bumps for each level
     * gained. Summons do not level. @return levels gained
     */
    public int grantXp(int amount) {
        if (isSummon() || amount <= 0) {
            return 0;
        }
        int gained = progression.addXp(amount);
        if (gained > 0) {
            stats.increaseMaxHp(gained * Growth.AUTO_HP_PER_LEVEL);
            stats.increaseSpeed(gained * Growth.AUTO_SPEED_PER_LEVEL);
        }
        unlockMovesForCurrentLevel();
        return gained;
    }

    public List<Move> getLastUnlockedMoves() {
        return List.copyOf(lastUnlockedMoves);
    }

    public MoveKit getKit() {
        return kit;
    }

    public boolean knowsMove(String moveName) {
        for (Move move : moves) {
            if (move.getName().equalsIgnoreCase(moveName)) {
                return true;
            }
        }
        return false;
    }

    public int getMoveRank(Move move) {
        if (move == null) {
            return 0;
        }
        return moveRanks.getOrDefault(move.getName(), 0);
    }

    /** Base power plus flat points. Utility moves with 0 power stay at 0 (no accidental damage). */
    public int effectivePower(Move move) {
        if (move == null || move.getPower() <= 0) {
            return 0;
        }
        return move.getPower() + getMoveRank(move) * Growth.POWER_PER_MOVE_POINT;
    }

    /** 1.0 + 5% per rank. Also used to scale heals. */
    public double moveScaling(Move move) {
        return 1.0 + getMoveRank(move) * Growth.SCALING_PER_MOVE_POINT;
    }

    /**
     * Spends one move point on a known move. @return new rank, or -1 if it could not be spent
     */
    public int spendMovePoint(Move move) {
        if (move == null || !knowsMove(move.getName()) || !progression.spendMovePoint()) {
            return -1;
        }
        int rank = getMoveRank(move) + 1;
        moveRanks.put(move.getName(), rank);
        return rank;
    }

    public boolean equip(Gear gear, Inventory inventory) {
        boolean equipped = loadout.equip(this, gear, inventory);
        if (equipped) {
            SetBonuses.refresh(this, loadout);
        }
        return equipped;
    }

    public Gear unequip(GearSlot slot, Inventory inventory) {
        Gear removed = loadout.unequip(this, slot, inventory);
        SetBonuses.refresh(this, loadout);
        return removed;
    }

    /**
     * Spends gold to raise a piece this character is wearing, or a piece
     * still in the bag. Equipped pieces apply the stat delta immediately.
     */
    public boolean upgradeGear(Gear gear, Inventory inventory, RandomProvider random) {
        if (gear == null || inventory == null || random == null) {
            return false;
        }
        boolean wearing = loadout.has(gear);
        if (!wearing && !inventory.contains(gear)) {
            return false;
        }
        int cost = Gear.upgradeCost(gear.getLevel());
        if (cost <= 0 || !inventory.trySpendGold(cost)) {
            return false;
        }
        GearLevelUp result = gear.levelUp(random);
        if (wearing) {
            applyGearDelta(result);
            SetBonuses.refresh(this, loadout);
        }
        return result.leveled();
    }

    void applyGearBonuses(Gear gear, int sign) {
        if (gear == null || sign == 0) {
            return;
        }
        for (StatKind kind : StatKind.values()) {
            int amount = gear.bonus(kind);
            if (amount != 0) {
                stats.add(kind, sign * amount);
            }
        }
    }

    void applyGearDelta(GearLevelUp result) {
        if (result == null) {
            return;
        }
        if (result.getMainDelta() != 0 && result.getMainKind() != null) {
            stats.add(result.getMainKind(), result.getMainDelta());
        }
        if (result.getSubDelta() != 0 && result.getSubstatKind() != null) {
            stats.add(result.getSubstatKind(), result.getSubDelta());
        }
    }

    private void unlockMovesForCurrentLevel() {
        lastUnlockedMoves.clear();
        if (kit == null || !kit.isGated()) {
            return;
        }
        int level = progression.getLevel();
        for (Move candidate : kit.unlockedAt(level)) {
            if (knowsMove(candidate.getName())) {
                continue;
            }
            addMove(candidate);
            lastUnlockedMoves.add(candidate);
        }
    }

    /**
     * Spends one unspent point. Specialty stats gain more. @return the amount added, or 0 if none left
     */
    public int spendStatPoint(StatKind kind) {
        if (kind == null || !kind.canSpendPoints() || !progression.spendStatPoint()) {
            return 0;
        }
        int gain = Growth.pointGain(kind, specialties.contains(kind));
        applyStatGain(kind, gain);
        return gain;
    }

    private void applyStatGain(StatKind kind, int gain) {
        switch (kind) {
            case HP:
                stats.increaseMaxHp(gain);
                break;
            case ATTACK:
                stats.increaseAttack(gain);
                break;
            case DEFENSE:
                stats.increaseDefense(gain);
                break;
            case MAGIC_ATTACK:
                stats.increaseMagicAttack(gain);
                break;
            case MAGIC_DEFENSE:
                stats.increaseMagicDefense(gain);
                break;
            case SPEED:
                stats.increaseSpeed(gain);
                break;
            default:
                break;
        }
    }

    /** Sheet / combat crit chance in percent (gear + passives). */
    public int getCritRate() {
        return getCritRate(null);
    }

    public int getCritRate(Move move) {
        int rate = stats.getCritRate();
        for (Passive passive : passives) {
            rate = passive.modifyCritRate(this, move, rate);
        }
        return Math.max(0, rate);
    }

    /** Sheet / combat crit damage in percent (gear + sets + passives). */
    public int getCritDamage() {
        return getCritDamage(null);
    }

    public int getCritDamage(Move move) {
        int damage = stats.getCritDamage();
        for (Passive passive : passives) {
            damage = passive.modifyCritDamage(this, move, damage);
        }
        return Math.max(0, damage);
    }

    /** 1.0 + crit damage%. 0% crit damage leaves a crit at 1.0x. */
    public double critMultiplier(Move move) {
        return 1.0 + getCritDamage(move) / 100.0;
    }

    public Status getStatus() {
        if (effects.isEmpty()) {
            return Status.NONE;
        }
        if (effects.size() == 1) {
            return effects.keySet().iterator().next();
        }
        for (Status status : Status.values()) {
            if (status != Status.NONE && effects.containsKey(status)) {
                return status;
            }
        }
        return Status.NONE;
    }

    public boolean hasStatus(Status status) {
        return status != null && status != Status.NONE && effects.containsKey(status);
    }

    public boolean hasAnyStatus() {
        return !effects.isEmpty();
    }

    /** Snapshot of statuses currently on this character, in enum order. */
    public List<Status> getActiveStatuses() {
        List<Status> active = new ArrayList<>();
        for (Status status : Status.values()) {
            if (status != Status.NONE && effects.containsKey(status)) {
                active.add(status);
            }
        }
        return active;
    }

    public StatusEffect getStatusEffect(Status status) {
        return effects.get(status);
    }

    public int getStatusTurnsRemaining() {
        return getStatusTurnsRemaining(getStatus());
    }

    public int getStatusTurnsRemaining(Status status) {
        StatusEffect effect = effects.get(status);
        return effect == null ? 0 : effect.getTurnsRemaining();
    }

    public int getStatusMagnitude() {
        return getStatusMagnitude(getStatus());
    }

    public int getStatusMagnitude(Status status) {
        StatusEffect effect = effects.get(status);
        return effect == null ? 0 : effect.getMagnitude();
    }

    public double getStatusEffectiveness(Status status) {
        StatusEffect effect = effects.get(status);
        return effect == null ? 1.0 : effect.getEffectiveness();
    }

    public int getQueuedSkipTurns() { return queuedSkipTurns; }

    /** Speed used by turn order. SLOW halves it, plus 15% per extra stack. */
    public int getEffectiveSpeed() {
        int speed = stats.getSpeed();
        if (!hasStatus(Status.SLOW)) {
            return speed;
        }
        double slowStrength = 0.5 * getStatusEffectiveness(Status.SLOW);
        return Math.max(1, (int) Math.round(speed * (1.0 - slowStrength)));
    }

    public void setStatus(Status status) {
        setStatus(status, 0);
    }

    /**
     * {@link Status#NONE} clears every effect. Any other status is applied
     * without wiping the others (stack or refresh per {@link Status}).
     */
    public void setStatus(Status status, int magnitude) {
        if (status == null || status == Status.NONE) {
            effects.clear();
            queuedSkipTurns = 0;
            return;
        }
        applyStatus(status, magnitude);
    }

    /**
     * @return {@code stacked} if an existing stackable status got stronger,
     *         {@code refreshed} if a reset-type was reapplied, {@code applied} if new
     */
    public String applyStatus(Status status, int magnitude) {
        if (status == null || status == Status.NONE) {
            return "none";
        }
        int duration = status.getDefaultDurationTurns();
        StatusEffect existing = effects.get(status);
        if (existing != null) {
            if (status.stacksOnReapply()) {
                existing.stackEffectiveness();
                return "stacked";
            }
            if (status == Status.PARALYSIS) {
                queuedSkipTurns = 0;
            }
            existing.resetToFresh(magnitude, duration);
            return "refreshed";
        }
        effects.put(status, new StatusEffect(status, magnitude, duration));
        return "applied";
    }

    public void clearStatus(Status status) {
        if (status == null || status == Status.NONE) {
            return;
        }
        effects.remove(status);
        if (status == Status.PARALYSIS) {
            queuedSkipTurns = 0;
        }
    }

    public void queueSkipTurns(int turns) {
        if (turns <= 0) {
            return;
        }
        queuedSkipTurns += turns;
    }

    /** True if this character must skip this action from a prior multi-turn paralysis proc. */
    public boolean consumeQueuedSkip() {
        if (queuedSkipTurns <= 0) {
            return false;
        }
        queuedSkipTurns--;
        return true;
    }

    /** Counts down one timed status. @return true if it expired */
    public boolean decrementStatusDuration(Status status) {
        StatusEffect effect = effects.get(status);
        if (effect == null) {
            return false;
        }
        if (effect.getTurnsRemaining() <= 0) {
            return false;
        }
        boolean expired = effect.decrementTurn();
        if (expired) {
            clearStatus(status);
        }
        return expired;
    }

    /** @deprecated use {@link #decrementStatusDuration(Status)} */
    @Deprecated
    public void decrementStatusDuration() {
        Status current = getStatus();
        if (current != Status.NONE) {
            decrementStatusDuration(current);
        }
    }

    public boolean isFainted() { return stats.isFainted(); }

    public Character getSiphonSource() { return siphonSource; }
    public int getSiphonTurnsRemaining() { return siphonTurnsRemaining; }
    public boolean isSiphoned() { return siphonTurnsRemaining > 0 && siphonSource != null; }

    public void applySiphon(Character source, int turns) {
        this.siphonSource = source;
        this.siphonTurnsRemaining = turns;
    }

    public void decrementSiphonDuration() {
        if (siphonTurnsRemaining <= 0) {
            return;
        }
        siphonTurnsRemaining--;
        if (siphonTurnsRemaining == 0) {
            siphonSource = null;
        }
    }

    public void clearSiphon() {
        siphonSource = null;
        siphonTurnsRemaining = 0;
    }

    public int getWoundStacks() {
        return woundStacks;
    }

    public int getWoundTurnsRemaining() {
        return woundTurnsRemaining;
    }

    public boolean isWounded() {
        return woundStacks > 0 && woundTurnsRemaining > 0;
    }

    /** Adds stacks (capped at {@link #WOUND_STACK_CAP}) and refreshes the window. */
    public void addWoundStacks(int stacks, int durationTurns) {
        if (stacks <= 0) {
            return;
        }
        this.woundStacks = Math.min(WOUND_STACK_CAP, this.woundStacks + stacks);
        this.woundTurnsRemaining = Math.max(1, durationTurns);
    }

    /** Clears stacks and returns how many were consumed. */
    public int consumeWoundStacks() {
        int stacks = woundStacks;
        woundStacks = 0;
        woundTurnsRemaining = 0;
        return stacks;
    }

    /** @return true if stacks expired this tick */
    public boolean decrementWoundDuration() {
        if (woundTurnsRemaining <= 0) {
            return false;
        }
        woundTurnsRemaining--;
        if (woundTurnsRemaining == 0) {
            woundStacks = 0;
            return true;
        }
        return false;
    }

    public boolean isSummon() { return summoner != null; }
    public Character getSummoner() { return summoner; }
    public void setSummoner(Character summoner) { this.summoner = summoner; }

    public Move getMoveByName(String name) {
        for (Move move : moves) {
            if (move.getName().equalsIgnoreCase(name)) {
                return move;
            }
        }
        throw new IllegalArgumentException(this.name + " doesn't know " + name);
    }

    public void addMove(Move move) {
        for (Move existing : moves) {
            if (existing.getName().equals(move.getName())) {
                return;
            }
        }
        moves.add(move);
    }

    public void removeMoveByName(String name) {
        moves.removeIf(move -> move.getName().equals(name));
    }

    public void addPassive(Passive passive) {
        if (!passives.contains(passive)) {
            passives.add(passive);
        }
    }

    public void removePassivesOfType(Class<?> type) {
        passives.removeIf(type::isInstance);
    }

    public boolean hasPassive(Class<?> type) {
        for (Passive passive : passives) {
            if (type.isInstance(passive)) {
                return true;
            }
        }
        return false;
    }

    public <T extends Passive> T getPassive(Class<T> type) {
        for (Passive passive : passives) {
            if (type.isInstance(passive)) {
                return type.cast(passive);
            }
        }
        return null;
    }

    public int getBlessedStacks() {
        return blessedStacks;
    }

    public void addBlessedStacks(int amount, int cap) {
        if (amount <= 0 || cap <= 0) {
            return;
        }
        blessedStacks = Math.min(cap, blessedStacks + amount);
    }

    public void clearBlessedStacks() {
        blessedStacks = 0;
    }

    /**
     * Puts the kit ult on cooldown so it cannot be used on the opening action.
     * Arena 1v1 / 3v3 fights start this way; dungeon ults come up when learned.
     */
    public void putUltOnCooldown() {
        if (kit == null) {
            return;
        }
        Move ult = kit.getUlt();
        if (ult == null || !knowsMove(ult.getName())) {
            return;
        }
        startMoveCooldown(ult.getName(), ult.getCooldownTurns());
    }

    public void startMoveCooldown(String moveName, int turns) {
        if (turns <= 0) {
            return;
        }
        moveCooldowns.put(moveName, turns);
    }

    public int getMoveCooldown(String moveName) {
        return moveCooldowns.getOrDefault(moveName, 0);
    }

    public void tickMoveCooldowns() {
        moveCooldowns.replaceAll((name, remaining) -> Math.max(0, remaining - 1));
        moveCooldowns.values().removeIf(remaining -> remaining <= 0);
    }
}