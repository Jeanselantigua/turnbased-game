# RPG Battle Sim

A generic turn-based RPG battle engine (Phase 1: no networking yet).

## How to import into Eclipse
1. Unzip this folder anywhere on disk.
2. In Eclipse: File > Import > General > Existing Projects into Workspace.
3. Click "Select root directory" and choose the unzipped `rpg-battle-sim` folder.
4. Make sure the project is checked, then click Finish.
5. Run `Main.java` (Run > Run As > Java Application) to confirm it builds.
6. Run the tests in `src/test/java` (Run > Run As > JUnit Test) to confirm JUnit is wired up.

## Structure
- `model/` — Character, Stats, Move, Type, Status (plain data classes)
- `engine/` — TypeChart, DamageCalculator, TurnResolver, Battle (the actual logic — currently stubs with TODOs)
- `util/` — RandomProvider (testable randomness wrapper)

## Where to start
Fill in `TypeChart.getMultiplier()` first, then `DamageCalculator.calculateDamage()`,
then `TurnResolver.resolveTurn()`, then wire it all together in `Battle.run()` and `Main.java`.
