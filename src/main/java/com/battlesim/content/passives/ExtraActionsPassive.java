package com.battlesim.content.passives;

import com.battlesim.model.Character;
import com.battlesim.model.Passive;

/** Boss (or elite) takes extra actions on the same turn. */
public class ExtraActionsPassive implements Passive {

    private final int extra;

    public ExtraActionsPassive(int extra) {
        this.extra = Math.max(0, extra);
    }

    @Override
    public int extraActionsPerTurn(Character self) {
        return extra;
    }
}
