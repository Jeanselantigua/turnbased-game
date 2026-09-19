package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import com.battlesim.content.PlayableCharacters;
import com.battlesim.content.passives.MonkPerfectEnlightenmentPassive;
import com.battlesim.content.passives.SionExplodingShieldPassive;
import com.battlesim.model.BattleContext;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SimpleAiMoveSelectorTest {

    private static Character dummyFoe() {
        return new Character("Foe", new Stats(200, 40, 10, 10, 10, 10),
                Type.PHYSICAL, List.of(new Move("Slash", Type.PHYSICAL, 50, 100, 0, false, Status.NONE, 0)));
    }

    private static BattleContext oneVOne(Character monk, Character foe) {
        return new BattleContext() {
            @Override
            public List<Character> alliesOf(Character character) {
                return character == foe ? List.of(foe) : List.of(monk);
            }

            @Override
            public List<Character> enemiesOf(Character character) {
                return character == foe ? List.of(monk) : List.of(foe);
            }

            @Override
            public void summonAlly(Character summoner, Character summon, List<String> log) {
            }
        };
    }

    private static void angerMonk(Character monk, Character foe) {
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        pe.onActionResolved(monk, monk.getMoveByName(MonkPerfectEnlightenmentPassive.MOVE_NAME),
                List.of(foe), oneVOne(monk, foe), new ArrayList<>());
        pe.onStatusReceived(monk, Status.STUN, foe, new ArrayList<>());
    }

    private static List<Move> angeredMoves(Character monk, Character foe) {
        MonkPerfectEnlightenmentPassive pe = monk.getPassive(MonkPerfectEnlightenmentPassive.class);
        return pe.filterOwnMoves(monk, monk.getMoves(), oneVOne(monk, foe));
    }

    private static RandomProvider alwaysHitsRng() {
        return new RandomProvider() {
            @Override
            public int nextInt(int min, int max) {
                return min;
            }

            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
    }

    @Test
    public void doesNotOpen1v1WithPerfectEnlightenment() {
        Character monk = PlayableCharacters.monk().createInstance();
        Character foe = dummyFoe();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        for (int i = 0; i < 20; i++) {
            ActionChoice choice = ai.chooseAction(monk, monk.getMoves(), List.of(foe), List.of(monk));
            assertNotEquals(MonkPerfectEnlightenmentPassive.MOVE_NAME, choice.getMove().getName());
        }
    }

    @Test
    public void swingsWhenAngeredAgainstHealthyFoe() {
        Character monk = PlayableCharacters.monk().createInstance();
        Character foe = dummyFoe();
        angerMonk(monk, foe);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(monk, angeredMoves(monk, foe), List.of(foe), List.of(monk));
        assertEquals(MonkPerfectEnlightenmentPassive.SWING_NAME, choice.getMove().getName());
    }

    @Test
    public void recoversWhenAngeredAgainstLowFoe() {
        Character monk = PlayableCharacters.monk().createInstance();
        Character foe = dummyFoe();
        angerMonk(monk, foe);
        foe.getStats().applyDamage(160);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(monk, angeredMoves(monk, foe), List.of(foe), List.of(monk));
        assertEquals(MonkPerfectEnlightenmentPassive.RECOVER_NAME, choice.getMove().getName());
    }

    @Test
    public void channelsIn1v1AfterTwoStaffHits() {
        Character monk = PlayableCharacters.monk().createInstance();
        Character foe = dummyFoe();
        BattleContext ctx = oneVOne(monk, foe);
        RandomProvider rng = alwaysHitsRng();
        TurnResolver hits = new TurnResolver(new DamageCalculator(new TypeChart(), rng), rng);
        hits.resolveAction(monk, new ActionChoice(monk.getMoveByName("Oochie"), List.of(foe)), ctx);
        hits.resolveAction(monk, new ActionChoice(monk.getMoveByName("Oochie"), List.of(foe)), ctx);

        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));
        ActionChoice choice = ai.chooseAction(monk, monk.getMoves(), List.of(foe), List.of(monk));
        assertEquals(MonkPerfectEnlightenmentPassive.MOVE_NAME, choice.getMove().getName());
    }

    @Test
    public void sionRoarsWhenShieldIsDown() {
        Character sion = PlayableCharacters.sion().createInstance();
        Character foe = dummyFoe();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(sion, sion.getMoves(), List.of(foe), List.of(sion));
        assertEquals(SionExplodingShieldPassive.MOVE_NAME, choice.getMove().getName());
        assertEquals(sion, choice.getTargets().get(0));
    }

    @Test
    public void sionAttacksWhenShieldIsUp() {
        Character sion = PlayableCharacters.sion().createInstance();
        Character foe = dummyFoe();
        sion.getStats().grantShield(100);
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(sion, sion.getMoves(), List.of(foe), List.of(sion));
        assertNotEquals(SionExplodingShieldPassive.MOVE_NAME, choice.getMove().getName());
    }

    @Test
    public void shieldsUnshieldedAllyOverSelfWhenAllyHasLowerHp() {
        Move barrier = new Move("Barrier", Type.HOLY, 80, 100, 0, true, Status.SHIELD, 100);
        Move bolt = new Move("Bolt", Type.HOLY, 40, 100, 0, true, Status.NONE, 0);
        Character caster = new Character("Caster", new Stats(200, 10, 10, 10, 10, 10),
                Type.HOLY, List.of(barrier, bolt));
        Character ally = dummyFoe();
        ally.getStats().applyDamage(50);
        Character foe = dummyFoe();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(caster, caster.getMoves(), List.of(foe), List.of(caster, ally));
        assertEquals("Barrier", choice.getMove().getName());
        assertEquals(ally, choice.getTargets().get(0));
    }

    @Test
    public void selfShieldDoesNotTargetAnUnshieldedAlly() {
        Character sion = PlayableCharacters.sion().createInstance();
        Character ally = dummyFoe();
        ally.getStats().applyDamage(50);
        Character foe = dummyFoe();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(sion, sion.getMoves(), List.of(foe), List.of(sion, ally));
        assertEquals(SionExplodingShieldPassive.MOVE_NAME, choice.getMove().getName());
        assertEquals(sion, choice.getTargets().get(0));
    }

    @Test
    public void channelsInTeamsWhenAlliesAreHealthy() {
        Character monk = PlayableCharacters.monk().createInstance();
        Character ally = dummyFoe();
        Character foe = dummyFoe();
        SimpleAiMoveSelector ai = new SimpleAiMoveSelector(new RandomProvider(1L));

        ActionChoice choice = ai.chooseAction(monk, monk.getMoves(), List.of(foe), List.of(monk, ally));
        assertEquals(MonkPerfectEnlightenmentPassive.MOVE_NAME, choice.getMove().getName());
    }
}
