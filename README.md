# RPG Battle Sim

Single-player turn-based RPG (Honkai Star Rail-style) with a Java battle engine and a JavaFX window.

## Eclipse
1. File > Import > General > Existing Projects into Workspace.
2. Select this folder (`rpg-battle-sim`).
3. Console: run `com.battlesim.Main`.
4. Windowed UI: run `com.battlesim.ui.GuiMain` (use the `GuiMain` launch config so JavaFX gets `--module-path lib/javafx --add-modules javafx.controls --enable-native-access=javafx.graphics`).
5. Tests: `src/test/java`, Run As > JUnit Test.

## Command line (Windows)
```
.\run-ui.ps1
```

JavaFX 26 Windows jars live in `lib/javafx/`. The engine does not need them — only the UI.

## Structure
- `model/` — Character, Stats, Move, gear, inventory
- `engine/` — damage, turns, Battle, BattleObserver
- `content/` — playable kits, enemies, passives, set bonuses
- `dungeon/` — floors, waypoints, camp
- `item/` — drops, gear factory
- `ui/` — JavaFX shell (Phase 5)
- `util/` — RandomProvider
