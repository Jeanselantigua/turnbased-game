package com.battlesim.content.passives;

import com.battlesim.engine.DamageCalculator;
import com.battlesim.engine.TypeChart;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Monk state machine for Perfect Enlightenment: Channel → Enlightened or Angered.
 *
     * Team channel grants damage resist (still targetable). If an ally falls
     * during the channel, the ritual fails and the monk becomes Angered.
 * opponent to disrupting moves. Enlightened staff hits deal reduced damage and
 * apply Blessed; Divine Blessing is typed Holy magic damage.
 */
public class MonkPerfectEnlightenmentPassive implements Passive {

    public enum Phase {
        IDLE,
        CHANNELING,
        ENLIGHTENED,
        ANGERED
    }

    public static final String MOVE_NAME = "Perfect Enlightenment";
    public static final String SWING_NAME = "Heavy Staff Swing";
    public static final String RECOVER_NAME = "Recover";

    public static final int CHANNEL_TURNS = 1;
    public static final int BLESSED_CAP = 3;
    public static final int COOLDOWN_TURNS = 1;
    public static final int SOLO_CHANNEL_MIN_HITS = 2;

    /** Incoming damage kept during a team channel (60% resist). */
    public static final double TEAM_CHANNEL_DAMAGE_RESIST = 0.60;
    /** Enlightened staff basics deal this fraction of normal damage. */
    public static final double ENLIGHTENED_STAFF_DAMAGE = 0.25;
    /** Y — debuff accuracy reduction during channel. */
    public static final double TEAM_DEBUFF_ACCURACY_REDUCTION = 0.40;
    public static final double SOLO_DEBUFF_ACCURACY_REDUCTION = 0.25;
    /** Z — counter damage as a fraction of the monk's attack. */
    public static final double TEAM_COUNTER_DAMAGE_RATIO = 0.35;
    public static final double SOLO_COUNTER_DAMAGE_RATIO = 0.25;

    public static final double TEAM_DODGE_BASE = 0.20;
    public static final double SOLO_DODGE_BASE = 0.15;
    public static final double TEAM_DODGE_STACK = 0.10;
    public static final double SOLO_DODGE_STACK = 0.08;
    public static final double TEAM_DODGE_CAP = 0.70;
    public static final double SOLO_DODGE_CAP = 0.50;

    public static final int DIVINE_DAMAGE_PER_STACK_TEAM = 22;
    public static final int DIVINE_DAMAGE_PER_STACK_SOLO = 16;

    /**
     * DECISION PENDING: (a) execute if target HP &lt; threshold, or
     * (b) burst only + a custom debuff TBD. Execute stays off for now.
     */
    public static boolean DIVINE_BLESSING_EXECUTE = true;
    public static final double EXECUTE_HP_THRESHOLD_TEAM = 0.15;
    public static final double EXECUTE_HP_THRESHOLD_SOLO = 0.10;

    public static final double ANGERED_CRIT_CHANCE = 0.60;
    public static final double ANGERED_EASY_BLOCK_CHANCE = 0.25;

    private final RandomProvider random;
    private final DamageCalculator damageCalculator;
    private Phase phase = Phase.IDLE;
    private boolean teamChannel;
    private int channelTurnsRemaining;
    private int idleHitsLanded;
    private MonkPerfectDodgePassive dodgePassive;
    private final List<Passive> storedPassives = new ArrayList<>();

    public MonkPerfectEnlightenmentPassive() {
        this(new RandomProvider());
    }

    public MonkPerfectEnlightenmentPassive(RandomProvider random) {
        this.random = random;
        this.damageCalculator = new DamageCalculator(new TypeChart(), random);
    }

    public static Move createMove() {
        return new Move(MOVE_NAME, Type.HOLY, 0, 100, 0, true, Status.NONE, 0, COOLDOWN_TURNS);
    }

    public static Move heavyStaffSwing() {
        return new Move(SWING_NAME, Type.PHYSICAL, 90, 70, 0, false, Status.NONE, 0);
    }

    public static Move recover() {
        return new Move(RECOVER_NAME, Type.HOLY, 0, 100, 0, true, Status.UTILITY, 0);
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isTeamChannel() {
        return teamChannel;
    }

    public int getChannelTurnsRemaining() {
        return channelTurnsRemaining;
    }

    public boolean isEnlightened() {
        return phase == Phase.ENLIGHTENED;
    }

    public int getIdleHitsLanded() {
        return idleHitsLanded;
    }

    public double dodgeBase() {
        return teamChannel ? TEAM_DODGE_BASE : SOLO_DODGE_BASE;
    }

    public double dodgeStack() {
        return teamChannel ? TEAM_DODGE_STACK : SOLO_DODGE_STACK;
    }

    public double dodgeCap() {
        return teamChannel ? TEAM_DODGE_CAP : SOLO_DODGE_CAP;
    }

    public double counterDamageRatio() {
        return teamChannel ? TEAM_COUNTER_DAMAGE_RATIO : SOLO_COUNTER_DAMAGE_RATIO;
    }

    public void applyBlessed(Character target, int stacks, List<String> log) {
        if (target == null || target.isFainted() || stacks <= 0) {
            return;
        }
        int before = target.getBlessedStacks();
        target.addBlessedStacks(stacks, BLESSED_CAP);
        int after = target.getBlessedStacks();
        if (after > before) {
            log.add(target.getName() + " gains Blessed (" + after + "/" + BLESSED_CAP + ")!");
        }
    }

    @Override
    public boolean skipsOwnAction(Character self) {
        return phase == Phase.CHANNELING;
    }

    @Override
    public void onActionSkipped(Character self, BattleContext context, List<String> log) {
        if (phase != Phase.CHANNELING) {
            return;
        }
        if (failChannelIfAllyFallen(self, context, log)) {
            return;
        }
        log.add(self.getName() + " continues channeling Perfect Enlightenment...");
        channelTurnsRemaining--;
        if (channelTurnsRemaining <= 0) {
            becomeEnlightened(self, log);
        }
    }

    @Override
    public void onFieldChanged(Character self, BattleContext context, List<String> log) {
        failChannelIfAllyFallen(self, context, log);
    }

    @Override
    public double modifyIncomingDamage(Character self, Character attacker, Move move,
                                        double damage, List<String> log) {
        if (phase == Phase.CHANNELING && teamChannel) {
            return damage * (1.0 - TEAM_CHANNEL_DAMAGE_RESIST);
        }
        return damage;
    }

    @Override
    public int modifyIncomingAccuracy(Character self, Character attacker, Move move, int accuracy) {
        if (phase != Phase.CHANNELING || !move.isDebuffCategory()) {
            return accuracy;
        }
        double reduction = teamChannel ? TEAM_DEBUFF_ACCURACY_REDUCTION : SOLO_DEBUFF_ACCURACY_REDUCTION;
        return (int) Math.round(accuracy * (1.0 - reduction));
    }

    @Override
    public double modifyOutgoingDamage(Character self, Character target, Move move,
                                        double damage, boolean isCrit, List<String> log) {
        if (phase == Phase.ENLIGHTENED && isStaffBasic(move)) {
            return damage * ENLIGHTENED_STAFF_DAMAGE;
        }
        if (phase == Phase.ANGERED && SWING_NAME.equals(move.getName())
                && random.nextDouble() < ANGERED_EASY_BLOCK_CHANCE) {
            log.add(target.getName() + " easily holds against the wild swing!");
            return 0;
        }
        return damage;
    }

    @Override
    public boolean rollBonusCrit(Character self, Move move) {
        return phase == Phase.ANGERED && SWING_NAME.equals(move.getName())
                && random.nextDouble() < ANGERED_CRIT_CHANCE;
    }

    @Override
    public void onAttackConnected(Character self, Character target, Move move,
                                   int damageDealt, boolean isCrit, boolean blocked,
                                   List<String> log, BattleContext context) {
        if (phase == Phase.IDLE && isStaffBasic(move)) {
            idleHitsLanded++;
        }
        if (phase != Phase.ENLIGHTENED || target == null) {
            return;
        }
        if (MOVE_NAME.equals(move.getName()) || SWING_NAME.equals(move.getName())
                || RECOVER_NAME.equals(move.getName())) {
            return;
        }
        int amount = isStaffBasic(move) ? 2 : 1;
        if (blocked) {
            amount /= 2;
        }
        applyBlessed(target, amount, log);
    }

    @Override
    public void onStatusReceived(Character self, Status status, Character source, List<String> log) {
        if (phase == Phase.CHANNELING && status == Status.STUN) {
            log.add(self.getName() + "'s channel is interrupted!");
            becomeAngered(self, log);
        }
    }

    @Override
    public void onActionResolved(Character self, Move move, List<Character> targets,
                                  BattleContext context, List<String> log) {
        if (RECOVER_NAME.equals(move.getName()) && phase == Phase.ANGERED) {
            recoverFromAngered(self, log);
            return;
        }
        if (!MOVE_NAME.equals(move.getName())) {
            return;
        }
        if (phase == Phase.IDLE) {
            startChannel(self, context, log);
        } else if (phase == Phase.ENLIGHTENED) {
            tryDivineBlessing(self, targets, log);
        } else {
            log.add(self.getName() + "'s Perfect Enlightenment has no effect.");
        }
    }

    @Override
    public List<Move> filterOwnMoves(Character self, List<Move> moves, BattleContext context) {
        List<Move> filtered = new ArrayList<>();
        if (phase == Phase.ANGERED) {
            for (Move move : moves) {
                if (SWING_NAME.equals(move.getName()) || RECOVER_NAME.equals(move.getName())) {
                    filtered.add(move);
                }
            }
            return filtered;
        }
        for (Move move : moves) {
            if (SWING_NAME.equals(move.getName()) || RECOVER_NAME.equals(move.getName())) {
                continue;
            }
            if (MOVE_NAME.equals(move.getName()) && !canCastEnlightenment(self, context)) {
                continue;
            }
            filtered.add(move);
        }
        return filtered;
    }

    @Override
    public List<Move> restrictOpponentMoves(Character self, Character opponent,
                                             List<Move> moves, BattleContext context) {
        if (phase != Phase.CHANNELING || teamChannel) {
            return moves;
        }
        List<Move> filtered = new ArrayList<>();
        for (Move move : moves) {
            if (move.isDebuffCategory()) {
                filtered.add(move);
            }
        }
        return filtered;
    }

    private boolean canCastEnlightenment(Character self, BattleContext context) {
        if (phase == Phase.IDLE) {
            return true;
        }
        if (phase != Phase.ENLIGHTENED || context == null) {
            return false;
        }
        return hasCappedEnemy(self, context);
    }

    private boolean hasCappedEnemy(Character self, BattleContext context) {
        for (Character enemy : context.enemiesOf(self)) {
            if (!enemy.isFainted() && enemy.getBlessedStacks() >= BLESSED_CAP) {
                return true;
            }
        }
        return false;
    }

    private void startChannel(Character self, BattleContext context, List<String> log) {
        teamChannel = hasLivingAlly(self, context);
        phase = Phase.CHANNELING;
        channelTurnsRemaining = CHANNEL_TURNS;
        idleHitsLanded = 0;
        if (teamChannel) {
            log.add(self.getName() + " begins Perfect Enlightenment and resists incoming damage!");
        } else {
            log.add(self.getName() + " begins Perfect Enlightenment! Opponents are limited to disrupting moves.");
        }
    }

    private void becomeEnlightened(Character self, List<String> log) {
        phase = Phase.ENLIGHTENED;
        channelTurnsRemaining = 0;
        storeAndRemove(self, MonkParryPassive.class);
        dodgePassive = new MonkPerfectDodgePassive(this, random);
        self.addPassive(dodgePassive);
        log.add(self.getName() + " reaches Perfect Enlightenment! Parry becomes Perfect Dodge.");
    }

    private void becomeAngered(Character self, List<String> log) {
        phase = Phase.ANGERED;
        channelTurnsRemaining = 0;
        if (dodgePassive != null) {
            self.removePassivesOfType(MonkPerfectDodgePassive.class);
            dodgePassive = null;
        }
        storeAndRemove(self, MonkParryPassive.class);
        storeAndRemove(self, MonkHolySplitPassive.class);
        self.addMove(heavyStaffSwing());
        self.addMove(recover());
        log.add(self.getName() + " is ANGERED! The monk kit is lost — only a heavy swing remains.");
    }

    private void recoverFromAngered(Character self, List<String> log) {
        phase = Phase.IDLE;
        idleHitsLanded = 0;
        self.removeMoveByName(SWING_NAME);
        self.removeMoveByName(RECOVER_NAME);
        for (Passive stored : storedPassives) {
            self.addPassive(stored);
        }
        storedPassives.clear();
        self.startMoveCooldown(MOVE_NAME, COOLDOWN_TURNS);
        log.add(self.getName() + " steadies their breath and recovers their kit.");
    }

    private void tryDivineBlessing(Character self, List<Character> targets, List<String> log) {
        boolean blessedAnyone = false;
        for (Character target : targets) {
            if (target.getBlessedStacks() < BLESSED_CAP) {
                continue;
            }
            int stacks = target.getBlessedStacks();
            target.clearBlessedStacks();
            int actual = dealDivineBlessingDamage(self, target, stacks, log);
            log.add(self.getName() + " unleashes Divine Blessing on " + target.getName()
                    + ", consuming " + stacks + " Blessed for " + actual + " damage!");

            if (DIVINE_BLESSING_EXECUTE && !target.isFainted()) {
                double hpFraction = target.getStats().getCurrentHp()
                        / (double) target.getStats().getMaxHp();
                double threshold = teamChannel ? EXECUTE_HP_THRESHOLD_TEAM : EXECUTE_HP_THRESHOLD_SOLO;
                if (hpFraction < threshold) {
                    target.getStats().applyDamage(target.getStats().getCurrentHp());
                    log.add("Divine Blessing executes " + target.getName() + "!");
                }
            }
            if (target.isFainted()) {
                log.add(target.getName() + " has fainted!");
            }
            blessedAnyone = true;
        }
        if (blessedAnyone) {
            self.startMoveCooldown(MOVE_NAME, COOLDOWN_TURNS);
        } else {
            log.add(self.getName() + "'s Perfect Enlightenment has no effect.");
        }
    }

    private int dealDivineBlessingDamage(Character self, Character target, int stacks,
                                          List<String> log) {
        int perStack = teamChannel ? DIVINE_DAMAGE_PER_STACK_TEAM : DIVINE_DAMAGE_PER_STACK_SOLO;
        int power = stacks * perStack
                + (int) Math.round(self.getStats().getMagicAttack() * 0.50);
        Move blessing = new Move(MOVE_NAME, Type.HOLY, Math.max(1, power), 100, 0, true, Status.NONE, 0);
        double damage = damageCalculator.calculateDamage(self, target, blessing);
        for (Passive passive : self.getPassives()) {
            damage = passive.modifyOutgoingDamage(self, target, blessing, damage, false, log);
        }
        for (Passive passive : target.getPassives()) {
            damage = passive.modifyIncomingDamage(target, self, blessing, damage, log);
        }
        return target.getStats().applyDamage((int) Math.round(Math.max(0, damage)));
    }

    private void storeAndRemove(Character self, Class<? extends Passive> type) {
        Passive existing = self.getPassive(type);
        if (existing != null) {
            storedPassives.add(existing);
            self.removePassivesOfType(type);
        }
    }

    private boolean failChannelIfAllyFallen(Character self, BattleContext context, List<String> log) {
        if (phase != Phase.CHANNELING || !teamChannel || context == null) {
            return false;
        }
        for (Character ally : context.alliesOf(self)) {
            if (ally != self && !ally.isSummon() && ally.isFainted()) {
                log.add(self.getName() + "'s Perfect Enlightenment fails as their ally falls!");
                becomeAngered(self, log);
                return true;
            }
        }
        return false;
    }

    private static boolean isStaffBasic(Move move) {
        String name = move.getName();
        return !MOVE_NAME.equals(name) && !SWING_NAME.equals(name) && !RECOVER_NAME.equals(name);
    }

    private static boolean hasLivingAlly(Character self, BattleContext context) {
        if (context == null) {
            return false;
        }
        for (Character ally : context.alliesOf(self)) {
            if (ally != self && !ally.isFainted() && !ally.isSummon()) {
                return true;
            }
        }
        return false;
    }
}
