package com.battlesim.item;

import com.battlesim.content.sets.BulwarkFourPiecePassive;
import com.battlesim.content.sets.SageFourPiecePassive;
import com.battlesim.content.sets.SwiftFourPiecePassive;
import com.battlesim.content.sets.VampireFourPiecePassive;
import com.battlesim.content.sets.VampireTwoPiecePassive;
import com.battlesim.content.sets.WarlordFourPiecePassive;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.Loadout;
import com.battlesim.model.Passive;
import com.battlesim.model.StatKind;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/** 2-piece / 4-piece set effects. Percent stats use attack after gear, excluding set bonuses. */
public final class SetBonuses {

    public static final double TWO_PIECE_PERCENT = 0.20;
    public static final double WARLORD_ONE_DEAD_ATTACK = 0.20;
    public static final double WARLORD_TWO_DEAD_ATTACK = 0.40;
    public static final double SAGE_SPEED_HP = 0.75;
    public static final double SAGE_SPEED_HP_HIGH = 0.50;
    public static final int SAGE_ALLY_SPEED_LOW = 10;
    public static final int SAGE_ALLY_SPEED_HIGH = 20;
    public static final int SWIFT_SPEED_TIER_1 = 60;
    public static final int SWIFT_SPEED_TIER_2 = 80;
    public static final double SWIFT_CRIT_TIER_1 = 0.20;
    public static final double SWIFT_CRIT_TIER_2 = 0.40;
    public static final double BULWARK_SHIELD_HP = 0.50;
    public static final double BULWARK_SHIELD_PERCENT = 0.40;
    public static final double VAMPIRE_LIFESTEAL = 0.25;
    public static final double VAMPIRE_KILL_MAX_HP = 0.045;

    private SetBonuses() {
    }

    public static void refresh(Character wearer, Loadout loadout) {
        if (wearer == null || loadout == null) {
            return;
        }
        Map<GearSet, Integer> counts = counts(loadout);
        applyTwoPieceStats(wearer, loadout, counts);
        syncPassive(wearer, VampireTwoPiecePassive.class, count(counts, GearSet.VAMPIRE) >= GearSet.TWO_PIECE,
                VampireTwoPiecePassive::new);
        syncPassive(wearer, WarlordFourPiecePassive.class, count(counts, GearSet.WARLORD) >= GearSet.FOUR_PIECE,
                WarlordFourPiecePassive::new);
        syncPassive(wearer, SageFourPiecePassive.class, count(counts, GearSet.SAGE) >= GearSet.FOUR_PIECE,
                SageFourPiecePassive::new);
        syncPassive(wearer, SwiftFourPiecePassive.class, count(counts, GearSet.SWIFT) >= GearSet.FOUR_PIECE,
                SwiftFourPiecePassive::new);
        syncPassive(wearer, BulwarkFourPiecePassive.class, count(counts, GearSet.BULWARK) >= GearSet.FOUR_PIECE,
                BulwarkFourPiecePassive::new);
        syncPassive(wearer, VampireFourPiecePassive.class, count(counts, GearSet.VAMPIRE) >= GearSet.FOUR_PIECE,
                VampireFourPiecePassive::new);
    }

    public static Map<GearSet, Integer> counts(Loadout loadout) {
        EnumMap<GearSet, Integer> counts = new EnumMap<>(GearSet.class);
        if (loadout == null) {
            return counts;
        }
        for (GearSlot slot : GearSlot.values()) {
            Gear piece = loadout.get(slot);
            if (piece == null) {
                continue;
            }
            GearSet set = piece.getSet();
            counts.put(set, counts.getOrDefault(set, 0) + 1);
        }
        return counts;
    }

    public static int count(Loadout loadout, GearSet set) {
        return count(counts(loadout), set);
    }

    public static int percentOf(int value, double percent) {
        if (value <= 0 || percent <= 0) {
            return 0;
        }
        return (int) Math.round(value * percent);
    }

    public static boolean belowFraction(Character character, double fraction) {
        if (character == null || fraction <= 0) {
            return false;
        }
        int max = character.getStats().getMaxHp();
        if (max <= 0) {
            return false;
        }
        return character.getStats().getCurrentHp() < max * fraction;
    }

    public static int sageAllySpeed(Character wearer) {
        if (wearer == null || wearer.isFainted()) {
            return 0;
        }
        if (belowFraction(wearer, SAGE_SPEED_HP_HIGH)) {
            return SAGE_ALLY_SPEED_HIGH;
        }
        if (belowFraction(wearer, SAGE_SPEED_HP)) {
            return SAGE_ALLY_SPEED_LOW;
        }
        return 0;
    }

    public static double swiftCritDamageBonus(int speed) {
        if (speed > SWIFT_SPEED_TIER_2) {
            return SWIFT_CRIT_TIER_2;
        }
        if (speed > SWIFT_SPEED_TIER_1) {
            return SWIFT_CRIT_TIER_1;
        }
        return 0;
    }

    public static int faintedAllies(Character self, BattleContext context) {
        if (self == null || context == null) {
            return 0;
        }
        int dead = 0;
        for (Character ally : context.alliesOf(self)) {
            if (ally == self || ally.isSummon()) {
                continue;
            }
            if (ally.isFainted()) {
                dead++;
            }
        }
        return dead;
    }

    public static int attackWithoutWarlordSet(Character wearer, WarlordFourPiecePassive fourPiece) {
        if (wearer == null) {
            return 0;
        }
        int extraFour = fourPiece == null ? 0 : fourPiece.getAppliedAttack();
        int extraTwo = wearer.getLoadout().twoPieceBonus(StatKind.ATTACK);
        return wearer.getStats().getAttack() - extraTwo - extraFour;
    }

    public static String summary(Loadout loadout) {
        Map<GearSet, Integer> counts = counts(loadout);
        if (counts.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (GearSet set : GearSet.values()) {
            int n = count(counts, set);
            if (n <= 0) {
                continue;
            }
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(set.getLabel()).append(" ").append(n);
        }
        return text.toString();
    }

    private static int count(Map<GearSet, Integer> counts, GearSet set) {
        return counts.getOrDefault(set, 0);
    }

    private static void applyTwoPieceStats(Character wearer, Loadout loadout, Map<GearSet, Integer> counts) {
        WarlordFourPiecePassive warlord4 = wearer.getPassive(WarlordFourPiecePassive.class);
        int warlordFour = warlord4 == null ? 0 : warlord4.getAppliedAttack();
        loadout.clearTwoPieceBonuses(wearer);

        if (count(counts, GearSet.WARLORD) >= GearSet.TWO_PIECE) {
            int base = wearer.getStats().getAttack() - warlordFour;
            loadout.addTwoPieceBonus(wearer, StatKind.ATTACK, percentOf(base, TWO_PIECE_PERCENT));
        }
        if (count(counts, GearSet.SAGE) >= GearSet.TWO_PIECE) {
            loadout.addTwoPieceBonus(wearer, StatKind.MAGIC_ATTACK,
                    percentOf(wearer.getStats().getMagicAttack(), TWO_PIECE_PERCENT));
        }
        if (count(counts, GearSet.SWIFT) >= GearSet.TWO_PIECE) {
            loadout.addTwoPieceBonus(wearer, StatKind.SPEED,
                    percentOf(wearer.getStats().getSpeed(), TWO_PIECE_PERCENT));
        }
        if (count(counts, GearSet.BULWARK) >= GearSet.TWO_PIECE) {
            loadout.addTwoPieceBonus(wearer, StatKind.DEFENSE,
                    percentOf(wearer.getStats().getDefense(), TWO_PIECE_PERCENT));
        }
    }

    private static <T extends Passive> void syncPassive(Character wearer, Class<T> type,
                                                        boolean shouldHave, Supplier<T> factory) {
        T existing = wearer.getPassive(type);
        if (shouldHave) {
            if (existing == null) {
                wearer.addPassive(factory.get());
            }
            return;
        }
        if (existing instanceof SetBonusPassive) {
            ((SetBonusPassive) existing).clearBonus(wearer);
        }
        wearer.removePassivesOfType(type);
    }
}
