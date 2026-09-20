package com.battlesim.content.passives;

import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import java.util.Set;

/** Attach on spawn for elites/bosses that ignore certain statuses. */
public class StatusImmunityPassive implements Passive {

    private final Set<Status> immuneTo;

    public StatusImmunityPassive(Set<Status> immuneTo) {
        this.immuneTo = Set.copyOf(immuneTo);
    }

    @Override
    public boolean isImmuneTo(Status status) {
        return immuneTo.contains(status);
    }
}
