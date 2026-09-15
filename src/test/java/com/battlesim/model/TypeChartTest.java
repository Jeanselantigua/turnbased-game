package com.battlesim.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.battlesim.engine.TypeChart;

public class TypeChartTest {

    @Test
    public void neutralMatchupReturnsOne() {
        TypeChart chart = new TypeChart();
        assertEquals(1.0, chart.getMultiplier(Type.PHYSICAL, Type.PHYSICAL), 0.001);
    }
}
