# RPG Battle Sim — Roadmap

**Project:** Java, plain Eclipse project, package `com.battlesim`  
**Current phase:** Phase 2 — Enemies, dungeon, climb sim

When a phase finishes or design debt is paid down, update this file **and** `.cursor/rules/project-roadmap.mdc` so future sessions stay in sync.

## Concept

Single-player turn-based RPG, Honkai Star Rail-style. Pre-selected character teams fight enemy teams. No networking, no switching/bench — the whole team fights at once. Speed-based Action Value turn order (faster characters act more often, not fixed rounds).

## Phase 1 — Core battle engine: DONE

- `model/`: Character, Stats, Move, Type, Status, Team, CharacterTemplate, Passive
- `engine/`: TypeChart, DamageCalculator, TurnResolver, TurnOrderScheduler, StatusEffectResolver, Battle, MoveSelector, ActionChoice
- `content/`: PlayableCharacters (registry), `content/passives/` (passive classes)
- `util/`: RandomProvider

## Passive system (built before Phase 2): DONE

- `Passive` is an interface with default no-op hook methods (`isUntargetable`, `modifyAccuracy`, `rollBonusCrit`, `modifyOutgoingDamage`, `modifyIncomingDamage`, `onHitLanded`, `onAttackMissed`, `onDamageTaken`)
- `CharacterTemplate` stores `List<Supplier<Passive>>` so each battle instance gets fresh, independent passive state (no shared mutable state)
- `TurnResolver.resolveHitOnTarget` calls every hook in order: accuracy → crit roll → base damage → outgoing mods → incoming mods → apply damage → `onDamageTaken` → `onHitLanded` → move's own status chance
- `Battle` filters enemy target lists by `isUntargetable()` (e.g. Rogue's stealth), with a fallback if it would otherwise softlock

## Combat rules (settled)

- **Bleed** snapshots the inflictor's attack as `Character.statusMagnitude` on apply (Knight passive and BLEED moves). Ticks deal 40% of that snapshot, not the afflicted character's attack.
- **Dual Swordsman statuses** are twice as effective: bleed snapshots 2× attack (ticks still 40% of that), and paralysis skip-locks for 2 turns when it procs.
- **Monk holy bonus** (`MonkHolySplitPassive`) adds 60% of magic attack after the mitigated hit, so it **ignores defense** by design.

## Phase 2 — Enemies, dungeon, climb sim: DONE

Do these in order. Do not fill later steps until the earlier ones actually fight.

1. **Enemy content** — `EnemyRank`, `EnemyTemplate` wrapping `CharacterTemplate`, `content/Enemies` (normals → elites → bosses). Dummy entries exist; replace with real kits.
2. **Boss engine hooks** — status immunities (`Passive.isImmuneTo`, wired in `TurnResolver`), extra actions (`Passive.extraActionsPerTurn`, wired in `Battle` without re-ticking DoT), phases as passives on HP thresholds (`onDamageTaken`).
3. **Dungeon mode** — `DungeonRun.run` fights waves with the same player `Character`s. `Dungeon.standard()` is 60 floors: normals (elites in the last 4 of each block), a boss every 15, and +0.15 enemy stats every 15 floors (not every wave). Solo / duo / trio multiply that scale by 0.75 / 0.90 / 1.00.
4. **BalanceSim climb** — DONE: `runDungeonProgress` prints avg / median / min / max / clear% for every solo, duo, and trio (`DUNGEON_RUNS_PER_COMP` = 5).

## Phase 3 — Leveling: NOT STARTED

XP, stat growth curves, move unlocks per level.

## Phase 4 — Items/equipment: NOT STARTED

Stat-boosting drops, drop tables, inventory.

## Phase 5 — Polish: NOT STARTED

Presentation/UI beyond console, README, resume packaging.
