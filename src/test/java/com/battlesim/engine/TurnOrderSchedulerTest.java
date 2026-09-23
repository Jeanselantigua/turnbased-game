package com.battlesim.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.battlesim.model.Character;
import com.battlesim.model.Move;
import com.battlesim.model.Stats;
import com.battlesim.model.Status;
import com.battlesim.model.Type;
import com.battlesim.util.RandomProvider;
import java.util.List;
import org.junit.Test;

public class TurnOrderSchedulerTest {

    @Test
    public void previewListsFasterFighterFirstWithoutChangingTheQueue() {
        Move slash = new Move("Slash", Type.PHYSICAL, 80, 100, 0, false, Status.NONE, 0);
        Character fast = new Character("Fast", new Stats(100, 20, 10, 1, 10, 100), Type.PHYSICAL, List.of(slash));
        Character slow = new Character("Slow", new Stats(100, 20, 10, 1, 10, 50), Type.PHYSICAL, List.of(slash));
        TurnOrderScheduler scheduler = new TurnOrderScheduler(List.of(fast, slow), new RandomProvider(1L));

        List<TurnOrderScheduler.UpcomingTurn> first = scheduler.preview(4);
        assertEquals(4, first.size());
        assertEquals("Fast", first.get(0).getCharacter().getName());
        assertEquals(0, first.get(0).delayFrom(first.get(0).getActionTime()));
        assertTrue(first.get(1).delayFrom(first.get(0).getActionTime()) > 0);

        Character next = scheduler.getNextActor();
        assertEquals("Fast", next.getName());
        List<TurnOrderScheduler.UpcomingTurn> still = scheduler.preview(1);
        assertEquals("Fast", still.get(0).getCharacter().getName());
    }
}
