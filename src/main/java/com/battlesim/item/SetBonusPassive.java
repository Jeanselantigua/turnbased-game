package com.battlesim.item;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;

/** A set bonus that must undo in-combat stat changes when the pieces come off. */
public interface SetBonusPassive extends Passive {

    void clearBonus(Character wearer);
}
