package rl.models.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import burlap.mdp.core.action.UniversalActionType;
import rts.units.UnitTypeTable;

public class ScriptActionTypes {
	
	//action names
	public static final String WORKER_RUSH = WorkerRush.class.getSimpleName();
	public static final String LIGHT_RUSH = LightRush.class.getSimpleName();
	public static final String RANGED_RUSH = RangedRush.class.getSimpleName();
	public static final String EXPAND = Expand.class.getSimpleName();
	public static final String BUILD_BARRACKS = BuildBarracks.class.getSimpleName();

	
	public static List<UniversalActionType> getActionTypes() {
		List<UniversalActionType> actionTypes = new ArrayList<>();
		 
		actionTypes.add(new UniversalActionType(WORKER_RUSH));
		actionTypes.add(new UniversalActionType(LIGHT_RUSH));
		actionTypes.add(new UniversalActionType(RANGED_RUSH));
		actionTypes.add(new UniversalActionType(EXPAND));
		actionTypes.add(new UniversalActionType(BUILD_BARRACKS));
		
		return actionTypes;
	}
	
	/**
	 * Returns a map from action names to {@link UniversalActionType}
	 * @return
	 */
	public static Map<String, UniversalActionType> getMapToActionTypes() {
		Map<String, UniversalActionType> actionTypeMap = new HashMap<>();
		 
		actionTypeMap.put(WORKER_RUSH, new UniversalActionType(WORKER_RUSH));
		actionTypeMap.put(LIGHT_RUSH, new UniversalActionType(LIGHT_RUSH));
		actionTypeMap.put(RANGED_RUSH, new UniversalActionType(RANGED_RUSH));
		actionTypeMap.put(EXPAND, new UniversalActionType(EXPAND));
		actionTypeMap.put(BUILD_BARRACKS, new UniversalActionType(BUILD_BARRACKS));
		
		return actionTypeMap;
	}
	
	/**
	 * Returns a map from action names to actual microRTS scripts, i.e., {@link AI}
	 * @param unitTypeTable
	 * @return
	 */
	public static Map<String, AI> getActionMapping(UnitTypeTable unitTypeTable){
		Map<String, AI> actions = new HashMap<>();
		
		actions.put(WORKER_RUSH, new WorkerRush(unitTypeTable));
		actions.put(LIGHT_RUSH, new LightRush(unitTypeTable));
		actions.put(RANGED_RUSH, new RangedRush(unitTypeTable));
		actions.put(EXPAND, new Expand(unitTypeTable));
		actions.put(BUILD_BARRACKS, new BuildBarracks(unitTypeTable));
		
		return actions;
	}

}
