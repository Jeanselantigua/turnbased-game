package com.battlesim.engine;

import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Passive;
import com.battlesim.model.Team;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;

public class Battle implements BattleContext {

    public static final int DEFAULT_MAX_ACTIONS = 200;

    private final Team teamA;
    private final Team teamB;
    private final MoveSelector selectorA;
    private final MoveSelector selectorB;
    private final TurnResolver turnResolver;
    private final StatusEffectResolver statusEffectResolver;
    private final TurnOrderScheduler scheduler;
    private final int maxActions;
    private final boolean verbose;
    private Character ritualFailedFor;

    public Battle(Team teamA, Team teamB,
                   MoveSelector selectorA, MoveSelector selectorB,
                   TurnResolver turnResolver,
                   StatusEffectResolver statusEffectResolver,
                   TurnOrderScheduler scheduler) {
        this(teamA, teamB, selectorA, selectorB, turnResolver, statusEffectResolver,
                scheduler, DEFAULT_MAX_ACTIONS, true);
    }

    public Battle(Team teamA, Team teamB,
                   MoveSelector selectorA, MoveSelector selectorB,
                   TurnResolver turnResolver,
                   StatusEffectResolver statusEffectResolver,
                   TurnOrderScheduler scheduler,
                   int maxActions, boolean verbose) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.selectorA = selectorA;
        this.selectorB = selectorB;
        this.turnResolver = turnResolver;
        this.statusEffectResolver = statusEffectResolver;
        this.scheduler = scheduler;
        this.maxActions = maxActions;
        this.verbose = verbose;
    }

    /** Wires TypeChart / damage / turns / scheduler from a shared RNG. */
    public static Battle create(Team teamA, Team teamB,
                                MoveSelector selectorA, MoveSelector selectorB,
                                RandomProvider random, int maxActions, boolean verbose) {
        TypeChart typeChart = new TypeChart();
        DamageCalculator damageCalculator = new DamageCalculator(typeChart, random);
        StatusEffectResolver statusEffectResolver = new StatusEffectResolver();
        TurnResolver turnResolver = new TurnResolver(damageCalculator, random, statusEffectResolver);
        List<Character> allCombatants = new ArrayList<>();
        allCombatants.addAll(teamA.getMembers());
        allCombatants.addAll(teamB.getMembers());
        TurnOrderScheduler scheduler = new TurnOrderScheduler(allCombatants, random);
        return new Battle(teamA, teamB, selectorA, selectorB, turnResolver,
                statusEffectResolver, scheduler, maxActions, verbose);
    }

    public BattleResult run() {
        List<String> fullLog = new ArrayList<>();
        int actionCount = 0;

        while (teamA.hasAnyAlive() && teamB.hasAnyAlive() && actionCount < maxActions) {
            Character actor = scheduler.getNextActor();
            if (actor == null) {
                break;
            }

            List<String> log = new ArrayList<>();
            statusEffectResolver.applyStartOfTurnEffects(actor, log);

            if (!actor.isFainted()) {
                actor.tickMoveCooldowns();
                for (Passive passive : new ArrayList<>(actor.getPassives())) {
                    passive.onTurnStart(actor, this, log);
                }

                boolean actorOnTeamA = teamA.getMembers().contains(actor);
                List<Character> enemies = actorOnTeamA ? teamB.getMembers() : teamA.getMembers();
                List<Character> allies = actorOnTeamA ? teamA.getMembers() : teamB.getMembers();
                MoveSelector selector = actorOnTeamA ? selectorA : selectorB;

                List<Character> livingEnemies = targetableOnly(enemies);
                if (livingEnemies.isEmpty()) {
                    List<Character> anyoneAlive = aliveOnly(enemies);
                    if (anyoneAlive.isEmpty()) {
                        break;
                    }
                    livingEnemies = anyoneAlive;
                }

                List<Character> livingAllies = aliveOnly(allies);
                if (skipsOwnAction(actor)) {
                    for (Passive passive : new ArrayList<>(actor.getPassives())) {
                        passive.onActionSkipped(actor, this, log);
                    }
                } else {
                    List<Move> available = availableMovesFor(actor);
                    if (available.isEmpty()) {
                        log.add(actor.getName() + " has no available actions!");
                    } else {
                        ActionChoice choice = selector.chooseAction(actor, available, livingEnemies, livingAllies);
                        log.addAll(turnResolver.resolveAction(actor, choice, this));
                    }
                }
            }
            faintOrphanSummons(log);
            if (checkRitualFailure(log)) {
                fullLog.addAll(log);
                if (verbose) {
                    log.forEach(System.out::println);
                }
                break;
            }
            fullLog.addAll(log);
            if (verbose) {
                log.forEach(System.out::println);
            }

            scheduler.advanceActor(actor);
            actionCount++;
        }

        BattleResult.Winner winner = determineWinner();
        if (verbose) {
            announceResult(winner);
        }

        return new BattleResult(winner, actionCount, snapshotFighters(), fullLog);
    }

    public List<Move> availableMovesFor(Character actor) {
        List<Move> moves = new ArrayList<>();
        for (Move move : actor.getMoves()) {
            if (actor.getMoveCooldown(move.getName()) > 0) {
                continue;
            }
            moves.add(move);
        }
        for (Passive passive : actor.getPassives()) {
            moves = new ArrayList<>(passive.filterOwnMoves(actor, moves, this));
        }
        for (Character enemy : enemiesOf(actor)) {
            if (enemy.isFainted()) {
                continue;
            }
            for (Passive passive : enemy.getPassives()) {
                moves = new ArrayList<>(passive.restrictOpponentMoves(enemy, actor, moves, this));
            }
        }
        return moves;
    }

    private boolean skipsOwnAction(Character actor) {
        for (Passive passive : actor.getPassives()) {
            if (passive.skipsOwnAction(actor)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRitualFailure(List<String> log) {
        for (Character character : teamA.getMembers()) {
            if (ritualFailed(character, log)) {
                ritualFailedFor = character;
                return true;
            }
        }
        for (Character character : teamB.getMembers()) {
            if (ritualFailed(character, log)) {
                ritualFailedFor = character;
                return true;
            }
        }
        return false;
    }

    private boolean ritualFailed(Character character, List<String> log) {
        for (Passive passive : character.getPassives()) {
            if (passive.forcesTeamLoss(character, this)) {
                log.add(character.getName() + "'s Perfect Enlightenment fails as their ally falls!");
                return true;
            }
        }
        return false;
    }

    private BattleResult.Winner determineWinner() {
        if (ritualFailedFor != null) {
            if (teamA.getMembers().contains(ritualFailedFor)) {
                return BattleResult.Winner.TEAM_B;
            }
            if (teamB.getMembers().contains(ritualFailedFor)) {
                return BattleResult.Winner.TEAM_A;
            }
        }
        boolean aAlive = teamA.hasAnyAlive();
        boolean bAlive = teamB.hasAnyAlive();
        if (aAlive && bAlive) {
            return BattleResult.Winner.TIMEOUT;
        }
        if (aAlive) {
            return BattleResult.Winner.TEAM_A;
        }
        if (bAlive) {
            return BattleResult.Winner.TEAM_B;
        }
        return BattleResult.Winner.DRAW;
    }

    @Override
    public List<Character> alliesOf(Character character) {
        if (teamA.getMembers().contains(character)) {
            return teamA.getMembers();
        }
        return teamB.getMembers();
    }

    @Override
    public List<Character> enemiesOf(Character character) {
        if (teamA.getMembers().contains(character)) {
            return teamB.getMembers();
        }
        return teamA.getMembers();
    }

    @Override
    public void summonAlly(Character summoner, Character summon, List<String> log) {
        summon.setSummoner(summoner);
        if (teamA.getMembers().contains(summoner)) {
            teamA.addMember(summon);
        } else {
            teamB.addMember(summon);
        }
        scheduler.addCombatant(summon);
        log.add(summoner.getName() + " summons " + summon.getName() + "!");
    }

    private void faintOrphanSummons(List<String> log) {
        faintOrphansOn(teamA, log);
        faintOrphansOn(teamB, log);
    }

    private void faintOrphansOn(Team team, List<String> log) {
        for (Character member : team.getMembers()) {
            if (!member.isSummon() || member.isFainted()) {
                continue;
            }
            Character owner = member.getSummoner();
            if (owner == null || owner.isFainted()) {
                member.getStats().applyDamage(member.getStats().getCurrentHp());
                log.add(member.getName() + " fades away!");
            }
        }
    }

    private List<BattleResult.FighterSnapshot> snapshotFighters() {
        List<BattleResult.FighterSnapshot> snapshots = new ArrayList<>();
        for (Character character : teamA.getMembers()) {
            snapshots.add(new BattleResult.FighterSnapshot(
                    "A", character.getName(),
                    character.getStats().getCurrentHp(),
                    character.getStats().getMaxHp()));
        }
        for (Character character : teamB.getMembers()) {
            snapshots.add(new BattleResult.FighterSnapshot(
                    "B", character.getName(),
                    character.getStats().getCurrentHp(),
                    character.getStats().getMaxHp()));
        }
        return snapshots;
    }

    private List<Character> aliveOnly(List<Character> characters) {
        List<Character> alive = new ArrayList<>();
        for (Character character : characters) {
            if (!character.isFainted()) {
                alive.add(character);
            }
        }
        return alive;
    }

    private List<Character> targetableOnly(List<Character> characters) {
        List<Character> targetable = new ArrayList<>();
        for (Character character : aliveOnly(characters)) {
            boolean hidden = false;
            for (Passive passive : character.getPassives()) {
                if (passive.isUntargetable(character)) {
                    hidden = true;
                    break;
                }
            }
            if (!hidden) {
                targetable.add(character);
            }
        }
        return targetable;
    }

    private void announceResult(BattleResult.Winner winner) {
        switch (winner) {
            case TEAM_A:
                System.out.println("Team A wins!");
                break;
            case TEAM_B:
                System.out.println("Team B wins!");
                break;
            case TIMEOUT:
                System.out.println("Battle timed out — no winner.");
                break;
            default:
                System.out.println("It's a draw — both teams were wiped!");
                break;
        }
    }
}
