# RPG Battle Sim — Roadmap

**Project:** Java, plain Eclipse project, package `com.battlesim`  
**Current phase:** Phase 5 — JavaFX UI (title, team pick, PvP, dungeon shell)

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

- **Bleed** snapshots the inflictor's attack as `statusMagnitude` on apply (BLEED moves). Ticks deal 40% of that snapshot, not the afflicted character's attack.
- **Dual Swordsman bleed** snapshots 1.5× attack (ticks still 40% of that). Paralysis uses the default 1-turn skip on proc.
- **Okirik Heal blessing** (`WomanBlessingPassive`) cleanses harmful statuses (including siphon and wounds) on the heal target, then reduces their next incoming hit.
- **Monk holy bonus** (`MonkHolySplitPassive`) adds 60% of magic attack after the mitigated hit, so it **ignores defense** by design.
- **Monk Enlightenment** lasts 6 of the monk's turns. Staff basics apply Blessed; **Divine Blessing** deals 100 + 8% of the target's max HP and extends Enlightenment by 2 only when 3/3 Blessed is consumed.
- **Monk ult** is **Master of Any Art**: copy the target's known kit ult (or their best damaging move if they have none, e.g. dungeon enemies). The copy is rettyped to Holy for STAB, never misses, effects always land, and source on-hit effects still fire. A copied ult can be used once; a copied basic can be used three times. Then the monk ult goes on cooldown.
- **Multiple statuses coexist** on one character (`EnumMap` of `StatusEffect`). All tick on that character's turn. Burn / poison / bleed / slow **stack** at +15% effectiveness per extra apply and do **not** extend duration. Stun / paralysis / curse / shield / self-shield **refresh** to a fresh copy (shields replace HP; stun skip clears only stun).

## Phase 2 — Enemies, dungeon, climb sim: DONE

Do these in order. Do not fill later steps until the earlier ones actually fight.

1. **Enemy content** — `EnemyRank`, `EnemyTemplate` wrapping `CharacterTemplate`, `content/Enemies` (normals → elites → bosses). Placeholder Elite Dummy / Boss Dummy kits are gone; dungeon waves use the real roster.
2. **Boss engine hooks** — status immunities (`Passive.isImmuneTo`, wired in `TurnResolver`), extra actions (`Passive.extraActionsPerTurn`, wired in `Battle` without re-ticking DoT), phases as passives on HP thresholds (`onDamageTaken`).
3. **Dungeon mode** — `DungeonRun.run` fights waves with the same player `Character`s. `Dungeon.standard()` is 100 floors: normals (elites in the last 4 of each block), a boss every 15, opening enemy stats at 0.85, then +0.20 every 15 floors (not every wave). Solo / duo / trio multiply that scale by 0.75 / 0.90 / 1.00. Every 10 floors (if the climb continues) the party **rests** (full heal, revive, clear statuses) or **opens a chest** (guaranteed gear + gold, no heal). Console play chooses; sims rest when anyone is fainted or under 70% HP.
4. **BalanceSim climb** — DONE: `runDungeonProgress` prints avg / median / min / max / clear% for every solo, duo, and trio (`DUNGEON_RUNS_PER_COMP` = 5).

## Phase 3 — Leveling: DONE (for now)

XP, stat points, specialty growth, dungeon XP, move unlocks, and move-point spending are wired. Augments wait for Phase 5 as a visible skill tree.

- **Starting stats** are ~half of the Phase 2 values so growth has room.
- **XP to next level** = `100 × current level`. Cap 30.
- **Wave XP** = sum of enemy rank bases (Normal 100 / Elite 200 / Boss 750) × dungeon difficulty (floor block + party-size scale). Whole party gets the wave amount (summons do not).
- **On level-up:** +4 HP and +1 Speed automatically (even if you skip those points — you'll still be fragile and slow), +1 stat point. After level 18, +1 unspent move point.
- **Stat point values:** HP +3/+4 specialty, combat stats +3/+4, Speed +2/+3. There is **no dungeon BST cap** — spend (or bank) points however you want. BST is just a sheet total. Sim AI still spreads leftover points with a slight specialty bias.
- **Sim / dungeon AI** spreads points across all six stats, plus one extra specialty slot per cycle (~50% more points in prio stats). Console play picks freely.
- **Specialties:** Mechanized Champion HP+DEF, Rogue SPD+ATK, Monk ATK+MATK, Caveman HP+ATK, Chef MATK+MDEF, Healer MATK+HP, Wizard MATK+SPD, Sion HP+DEF, Dual Swordsman ATK+MATK.
- **Moves:** every playable has a 4-move ladder — starter at 1, 2nd at 6, 3rd at 12, ult at 18 (3-turn cooldown). Dungeon characters start with only the first move.
- **Arena sim:** BalanceSim 1v1 / 3v3 uses Phase 2 stats on the current 4-move kits, with ults starting on cooldown. Dungeon climb still uses level-1 stats and unlocks moves while leveling.
- **Move points** spent on a known move: +5 power and +5% scaling (heals use the scaling; 0-power utilities stay 0 damage). AI dumps points into the ult. Console prompts.

## Phase 4 — Items/equipment: DONE

Inventory, ZZZ-style gear rolls, dungeon drops, and a console character menu.

- **Six slots:** Helm (HP), Gloves (ATK), Chest (DEF), Boots (SPD), Amulet (MATK or MDEF), Ring (any). Sets: Warlord, Sage, Swift, Bulwark, Vampire.
- **Set bonuses:** 2-piece / 4-piece, refreshed on equip/unequip. 2-piece is +20% ATK (Warlord), MATK (Sage), SPD (Swift), or DEF (Bulwark); Vampire 2-piece is 25% physical lifesteal. 4-piece: Warlord extra ATK as allies fall, Sage ally speed below 75%/50% HP, Swift crit damage above 60/80 SPD, Bulwark panic shield at 50% HP, Vampire +4.5% max HP per kill.
- **Main + 5 substats:** main grows every enhance level. Substats roll every 3 levels through +30 — a new line until five, then a random existing line is upgraded. Main kind never appears as a substat. Crit Rate and Crit DMG can roll as substats (not mains, not level-up points).
- **Rarity starting subs:** Common 0, Rare 1, Epic 2, Legendary 3. Cap is still five substats (later levels upgrade existing lines).
- **Inventory** is a party bag (unequipped pieces + gold). Each character has a 6-slot loadout; equip/unequip applies/removes stats on `Character`.
- **Drops:** gold and chance-by-rank gear after each cleared wave (Normal 20%, Elite 75%, Boss guaranteed). BalanceSim equips the bag after each wave, preferring 2pc/4pc of the kit's set (Warlord / Sage / Swift / Bulwark), then upgrades worn pieces.
- **Enhance cost:** cap +30. Total gold to max is still 3000 (the old +15 curve), spread so later levels cost more.
- **Console camp** between dungeon waves: view bag, equip/unequip, upgrade, sell (scrap + full upgrade-gold refund), leftover move points. Every 10 floors: rest (full heal / revive) or chest (guaranteed gear).

## Phase 5 — Polish / visible game: IN PROGRESS

JavaFX window on the existing engine (not a Godot/Unity port). Console `Main` still works.

- **Shell:** `com.battlesim.ui.GuiMain` — title, team pick, PvP vs AI, dungeon climb.
- **Battle UI:** HP cards, combat log, click-to-pick moves/targets, right-side AV turn queue. `BattleObserver` pushes log, field, and queue; `FxMoveSelector` blocks the battle thread until a click.
- **Dungeon camp (rough):** waypoint rest/chest, then camp for equip / unequip / upgrade / sell and leftover stat/move points. Dungeon GUI banks points (`BankStatAllocator`) instead of auto-spending.
- **Still to do:** polish camp, augment skill tree, README/resume packaging.

Run with `--module-path lib/javafx --add-modules javafx.controls --enable-native-access=javafx.graphics`, or `.\run-ui.ps1`.
