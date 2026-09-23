package com.battlesim.item;

import com.battlesim.model.Character;
import com.battlesim.model.Gear;
import com.battlesim.model.GearSet;
import com.battlesim.model.GearSlot;
import com.battlesim.model.Inventory;
import com.battlesim.model.Loadout;
import com.battlesim.model.StatKind;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Sim camp loadout: fill slots from the party bag, preferring 2-piece / 4-piece
 * of each kit's set, then spend gold upgrading those pieces.
 */
public final class SimGearAllocator {

    static final int SET_MATCH = 80;
    static final int TWO_PIECE = 120;
    static final int FOUR_PIECE = 220;

    private SimGearAllocator() {
    }

    public static void camp(List<Character> party, Inventory bag, RandomProvider random) {
        outfit(party, bag);
        upgrade(party, bag, random);
    }

    /**
     * ATK kits → Warlord, MATK (no ATK) → Sage, Speed → Swift, HP/DEF → Bulwark.
     * Wizard (MATK+SPD) takes Sage; Rogue (SPD+ATK) takes Swift.
     */
    public static GearSet preferredSet(Character character) {
        if (character == null) {
            return GearSet.VAMPIRE;
        }
        List<StatKind> specs = character.getSpecialties();
        if (specs.contains(StatKind.MAGIC_ATTACK) && !specs.contains(StatKind.ATTACK)) {
            return GearSet.SAGE;
        }
        if (specs.contains(StatKind.SPEED)) {
            return GearSet.SWIFT;
        }
        if (specs.contains(StatKind.ATTACK)) {
            return GearSet.WARLORD;
        }
        if (specs.contains(StatKind.DEFENSE) || specs.contains(StatKind.HP)) {
            return GearSet.BULWARK;
        }
        return GearSet.VAMPIRE;
    }

    public static void outfit(List<Character> party, Inventory bag) {
        if (party == null || bag == null) {
            return;
        }
        for (Character member : party) {
            equipSlots(member, bag, true);
        }
        for (int i = party.size() - 1; i >= 0; i--) {
            equipSlots(party.get(i), bag, false);
        }
    }

    private static void equipSlots(Character member, Inventory bag, boolean preferredOnly) {
        if (member == null || member.isSummon()) {
            return;
        }
        GearSet preferred = preferredSet(member);
        for (GearSlot slot : GearSlot.values()) {
            Gear pick = bestPiece(member, slot, preferred, bag, preferredOnly);
            if (pick != null) {
                member.equip(pick, bag);
            }
        }
    }

    public static void upgrade(List<Character> party, Inventory bag, RandomProvider random) {
        if (party == null || bag == null || random == null) {
            return;
        }
        boolean upgraded = true;
        while (upgraded) {
            upgraded = false;
            Character owner = null;
            Gear pick = null;
            int bestRank = Integer.MAX_VALUE;
            for (Character member : party) {
                if (member == null || member.isSummon()) {
                    continue;
                }
                GearSet preferred = preferredSet(member);
                for (GearSlot slot : GearSlot.values()) {
                    Gear piece = member.getLoadout().get(slot);
                    if (piece == null || piece.getLevel() >= Gear.MAX_LEVEL) {
                        continue;
                    }
                    int cost = Gear.upgradeCost(piece.getLevel());
                    if (cost <= 0 || bag.getGold() < cost) {
                        continue;
                    }
                    int rank = (piece.getSet() == preferred ? 0 : 1_000) + piece.getLevel();
                    if (rank < bestRank) {
                        bestRank = rank;
                        owner = member;
                        pick = piece;
                    }
                }
            }
            if (owner != null && pick != null && owner.upgradeGear(pick, bag, random)) {
                upgraded = true;
            }
        }
    }

    private static Gear bestPiece(Character wearer, GearSlot slot, GearSet preferred,
                                  Inventory bag, boolean preferredOnly) {
        Gear current = wearer.getLoadout().get(slot);
        int bestScore = current == null ? Integer.MIN_VALUE : score(current, wearer, preferred, slot);
        Gear best = null;
        for (Gear piece : new ArrayList<>(bag.getPieces())) {
            if (piece.getSlot() != slot) {
                continue;
            }
            if (preferredOnly && piece.getSet() != preferred) {
                continue;
            }
            int value = score(piece, wearer, preferred, slot);
            if (value > bestScore) {
                bestScore = value;
                best = piece;
            }
        }
        return best;
    }

    static int score(Gear piece, Character wearer, GearSet preferred, GearSlot slot) {
        if (piece == null) {
            return Integer.MIN_VALUE;
        }
        int value = piece.getRarity().ordinal() * 15
                + piece.getLevel() * 2
                + piece.getSubstats().size() * 3
                + mainWeight(wearer, piece.getMainKind());
        int setCount = setCountIfSlotIs(wearer.getLoadout(), slot, piece);
        if (piece.getSet() == preferred) {
            value += SET_MATCH;
            if (setCount >= GearSet.FOUR_PIECE) {
                value += FOUR_PIECE;
            } else if (setCount >= GearSet.TWO_PIECE) {
                value += TWO_PIECE;
            }
        } else if (setCount >= GearSet.FOUR_PIECE) {
            value += FOUR_PIECE / 2;
        } else if (setCount >= GearSet.TWO_PIECE) {
            value += TWO_PIECE / 2;
        }
        return value;
    }

    private static int setCountIfSlotIs(Loadout loadout, GearSlot slot, Gear piece) {
        int n = 0;
        for (GearSlot other : GearSlot.values()) {
            Gear worn = other == slot ? piece : loadout.get(other);
            if (worn != null && worn.getSet() == piece.getSet()) {
                n++;
            }
        }
        return n;
    }

    private static int mainWeight(Character wearer, StatKind kind) {
        if (kind == null) {
            return 0;
        }
        if (wearer.getSpecialties().contains(kind)) {
            return 30;
        }
        if (kind == StatKind.HP || kind == StatKind.SPEED) {
            return 12;
        }
        return 8;
    }
}
