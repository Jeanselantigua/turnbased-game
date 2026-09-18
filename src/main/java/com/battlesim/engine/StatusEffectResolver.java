package com.battlesim.engine;

import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Status;
import java.util.List;

/**
 * Applies status effects: end-of-turn DoT, aftermath on death, ally heal, and leech lifesteal.
 */
public class StatusEffectResolver {

    private static final double BURN_PERCENT_MAX_HP = 0.10;
    private static final double POISON_PERCENT_MAX_HP = 0.08;
    private static final double BLEED_PERCENT_ATTACK = 0.40;
    private static final double CURSED_PERCENT_MAGIC_ATTACK = 0.15;
    private static final double AFTERMATH_PERCENT_MAX_HP = 0.15;
    private static final double HEAL_PERCENT_MAX_HP = 0.25;
    private static final double LEECH_PERCENT_DAMAGE = 0.25;
    private static final double SIPHON_PERCENT_MAX_HP = 0.08;
    private static final double SIPHON_HEAL_PERCENT = 0.50;

    public void applyEndOfTurnEffects(Character character, List<String> log) {
        if (character.isFainted()) {
            return;
        }

        applySiphonTick(character, log);
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
            case CURSED:
                damage = (int) Math.round(character.getStats().getMagicAttack() * CURSED_PERCENT_MAGIC_ATTACK);
                break;
            default:
                return; // STUN/PARALYSIS/AFTERMATH/NONE do nothing here
        }

        if (damage > 0) {
            int actualDamage = character.getStats().applyDamage(damage);
            log.add(character.getName() + " takes " + actualDamage + " damage from " + status.toString().toLowerCase() + "!");
            if (character.isFainted()) {
                log.add(character.getName() + " has fainted!");
            }
        }

        if (status == Status.CURSED) {
            character.decrementStatusDuration();
            if (character.getStatus() == Status.NONE) {
                log.add(character.getName() + " is no longer cursed!");
            }
        }
        if (status == Status.BURN) {
            character.decrementStatusDuration();
            if (character.getStatus() == Status.NONE) {
                log.add(character.getName() + " is no longer burning!");
            }
        }
        if (status == Status.POISON) {
            character.decrementStatusDuration();
            if (character.getStatus() == Status.NONE) {
                log.add(character.getName() + " is no longer poisoned!");
            }
        }
        if (status == Status.BLEED) {
            character.decrementStatusDuration();
            if (character.getStatus() == Status.NONE) {
                log.add(character.getName() + " is no longer bleeding!");
            }
        }
    }

    private void applySiphonTick(Character character, List<String> log) {
        if (!character.isSiphoned()) {
            return;
        }

        int damage = (int) Math.round(character.getStats().getMaxHp() * SIPHON_PERCENT_MAX_HP);
        if (damage > 0) {
            int actualDamage = character.getStats().applyDamage(damage);
            log.add(character.getName() + " takes " + actualDamage + " damage from siphon!");
            Character source = character.getSiphonSource();
            if (source != null && !source.isFainted()) {
                int stolen = (int) Math.round(actualDamage * SIPHON_HEAL_PERCENT);
                if (stolen > 0) {
                    int missing = Math.max(0, source.getStats().getMaxHp() - source.getStats().getCurrentHp());
                    int healed = source.getStats().heal(Math.min(stolen, missing));
                    if (healed > 0) {
                        log.add(source.getName() + " siphons " + healed + " HP from "
                                + character.getName() + "!");
                        notifyHealed(source, healed, log);
                    }
                }
            }
            if (character.isFainted()) {
                log.add(character.getName() + " has fainted!");
            }
        }

        character.decrementSiphonDuration();
        if (!character.isSiphoned()) {
            log.add(character.getName() + " is no longer siphoned!");
        }
    }

    /**
     * When a character with AFTERMATH faints from a hit, the killer takes
     * 15% of the fainted character's max HP.
     */
    public void applyOnFaintEffects(Character fainted, Character killer, List<String> log) {
        if (fainted.getStatus() != Status.AFTERMATH) {
            return;
        }
        if (killer == null || killer == fainted || killer.isFainted()) {
            return;
        }

        int damage = (int) Math.round(fainted.getStats().getMaxHp() * AFTERMATH_PERCENT_MAX_HP);
        if (damage <= 0) {
            return;
        }

        int actualDamage = killer.getStats().applyDamage(damage);
        log.add(killer.getName() + " takes " + actualDamage + " damage from "
                + fainted.getName() + "'s aftermath!");
        if (killer.isFainted()) {
            log.add(killer.getName() + " has fainted!");
        }
    }

    public void applyHeal(Character target, List<String> log) {
        int amount = (int) Math.round(target.getStats().getMaxHp() * HEAL_PERCENT_MAX_HP);
        int healed = healUpToMax(target, amount);
        log.add(target.getName() + " recovers " + healed + " HP!");
        notifyHealed(target, healed, log);
    }

    public void applyLeechOnDamage(Character attacker, Move move, int damageDealt, List<String> log) {
        if (move.getInflictedStatus() != Status.LEECH || damageDealt <= 0 || attacker.isFainted()) {
            return;
        }
        int stolen = (int) Math.round(damageDealt * LEECH_PERCENT_DAMAGE);
        if (stolen <= 0) {
            return;
        }
        int healed = healUpToMax(attacker, stolen);
        if (healed > 0) {
            log.add(attacker.getName() + " leeches " + healed + " HP!");
            notifyHealed(attacker, healed, log);
        }
    }

    private void notifyHealed(Character character, int amount, List<String> log) {
        if (amount <= 0) {
            return;
        }
        for (Passive passive : character.getPassives()) {
            passive.onHealed(character, amount, log);
        }
    }

    private int healUpToMax(Character character, int amount) {
        int missingHp = Math.max(0, character.getStats().getMaxHp() - character.getStats().getCurrentHp());
        return character.getStats().heal(Math.min(amount, missingHp));
    }
}
