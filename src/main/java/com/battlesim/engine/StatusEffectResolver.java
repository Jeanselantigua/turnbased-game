package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Status;
import java.util.List;

/**
 * Applies end-of-turn damage-over-time effects (burn, poison, bleed).
 * Called once per acting Character, after their action resolves.
 */
public class StatusEffectResolver {

    private static final double BURN_PERCENT_MAX_HP = 0.10;
    private static final double POISON_PERCENT_MAX_HP = 0.08;
    private static final double BLEED_PERCENT_ATTACK = 0.20;

    public void applyEndOfTurnEffects(Character character, List<String> log) {
        if (character.isFainted()) {
            return;
        }

        Status status = character.getStatus();
        int damage;

        switch (status) {
            case BURN:
                damage = (int) Math.round(character.getStats().getMaxHp() * BURN_PERCENT_MAX_HP);
                break;
            case POISON:
                damage = (int) Math.round(character.getStats().getMaxHp() * POISON_PERCENT_MAX_HP);
                break;
            case BLEED:
                damage = (int) Math.round(character.getStats().getAttack() * BLEED_PERCENT_ATTACK);
                break;
            default:
                return; // STUN/PARALYSIS/NONE do nothing here — handled in TurnResolver
        }

        if (damage > 0) {
            int actualDamage = character.getStats().applyDamage(damage);
            log.add(character.getName() + " takes " + actualDamage + " damage from " + status + "!");
            if (character.isFainted()) {
                log.add(character.getName() + " has fainted!");
            }
        }
    }
}
