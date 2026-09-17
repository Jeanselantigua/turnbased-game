package com.battlesim.engine;

import com.battlesim.model.Type;
import java.util.EnumMap;
import java.util.Map;

/**
 * Looks up damage multipliers between an attacking Move's Type and a
 * defending Character's affinity Type.
 * TODO: fill in the multiplier table.
 */
public class TypeChart {
	
	private final Map<Type, Map<Type, Double>> chart = new EnumMap <>(Type.class);
	
	public TypeChart () {
		
		for (Type type : Type.values()) {
			chart.put(type, new EnumMap<>(Type.class));
		}
		
		setEffectiveness(Type.PHYSICAL, Type.HOLY, 2.0);
		setEffectiveness(Type.PHYSICAL, Type.ARCANE, 2.0);
		setEffectiveness(Type.PHYSICAL, Type.SHADOW, 0.0);
		setEffectiveness(Type.PHYSICAL, Type.UNDEAD, 0.5);
		
		setEffectiveness(Type.FIRE, Type.ICE, 2.0);
        setEffectiveness(Type.FIRE, Type.EARTH, 0.5);
        
        setEffectiveness(Type.ICE, Type.EARTH, 2.0);
        setEffectiveness(Type.ICE, Type.FIRE, 0.5);
        
        setEffectiveness(Type.LIGHTNING, Type.EARTH, 0.0); // grounded — immune
        setEffectiveness(Type.LIGHTNING, Type.PHYSICAL, 2.0);
        
        setEffectiveness(Type.HOLY, Type.UNDEAD, 2.0);
        setEffectiveness(Type.HOLY, Type.SHADOW, 2.0);
        
        setEffectiveness(Type.ARCANE, Type.PHYSICAL, 2.0);
        setEffectiveness(Type.ARCANE, Type.UNDEAD, 0.5);
        
        setEffectiveness(Type.SHADOW, Type.HOLY, 2.0);
        setEffectiveness(Type.SHADOW, Type.ARCANE, 0.5);
		
	}
	
	private void setEffectiveness(Type attacker, Type defender, double multiplier) {
        chart.get(attacker).put(defender, multiplier);
    }

    public double getMultiplier(Type attackingType, Type defendingType) {
    	return chart.get(attackingType).getOrDefault(defendingType, 1.0);
    }
}
