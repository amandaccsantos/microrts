package rl.models.simplecounting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.state.State;
import rl.models.stages.GameStage;
import rts.GameState;
import rts.Player;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 * This class groups quantities of state features as 'aggregates'
 * i.e. few, fair and many. 
 * How many does constitute each aggregate is empirically defined
 * 
 * @author anderson
 *
 */
public class AggregateState extends GameStage{
	
	public static final String KEY_WORKERS = "WORKERS";
	public static final String KEY_LIGHT = "LIGHT";
	public static final String KEY_RANGED = "RANGED";
	public static final String KEY_HEAVY = "HEAVY";
	public static final String KEY_BASES = "BASES";
	public static final String KEY_BARRACKS = "BARRACKS";
	public static final String KEY_RESOURCES = "RESOURCES";
	
	public static final String PLAYER = "PLAYER";
	public static final String OPPONENT = "OPPONENT";
	
	/**
	 * One map of variable-key to value per player 
	 */
	protected Map<Integer, Map<String, Object>> playerFeatures;
	
	/**
	 * One map of string->object per common state features
	 */
	protected Map<String, Object> stateVariables;
	
	
	protected static List<Object> keys;
	

	public AggregateState(GameState gameState) {
		super(gameState);
		
		//fills the state variables into a map
		stateVariables = new HashMap<>();
		stateVariables.put(KEY_STAGE, frameToStage(gameState.getTime()));
		
		// creates player features and fills it for each player
		playerFeatures = new HashMap<>();
		
		for(Player player : gameState.getPhysicalGameState().getPlayers()){
			// fills state features of current player
			Map<String, Object> currentPlayerFeatures = new HashMap<>();
			
			Aggregator aggr = new Aggregator();
			
			// put aggregates of units
			currentPlayerFeatures.put(
				KEY_WORKERS, aggr.aggregateUnits(countUnits("Worker", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_LIGHT, aggr.aggregateUnits(countUnits("Light", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_RANGED, aggr.aggregateUnits(countUnits("Ranged", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_HEAVY, aggr.aggregateUnits(countUnits("Heavy", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_BASES, aggr.aggregateBuildings(countUnits("Base", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_BARRACKS, aggr.aggregateBuildings(countUnits("Barracks", player.getID()))
			);
			currentPlayerFeatures.put(
				KEY_RESOURCES, aggr.aggregateResources(player.getResources())
			);
			
			playerFeatures.put(player.getID(), currentPlayerFeatures);
		}
		
	}
	
	public Object getPlayerFeature(int playerID, String featureName){
		return playerFeatures.get(playerID).get(featureName);
	}
	
	/**
	 * Counts the units of a type that a player has
	 * @param unitTypeName
	 * @param playerID
	 * @return
	 */
	public int countUnits(String unitTypeName, int playerID){
		int count = 0;
		for(Unit u : underlyingState.getUnits()){
			if(u.getType().name.equals(unitTypeName)) {
				count++;
			}
		}
		
		return count;
	}

	@Override
	public List<Object> variableKeys() {
		if(keys == null) {
			// adds keys for common state features
			keys = super.variableKeys();
			
			// adds keys for each player feature, key is playerID;feature
			for(Player player : underlyingState.getPhysicalGameState().getPlayers()){
				keys.add(String.format("%d-%s", player.getID(), KEY_WORKERS));
				keys.add(String.format("%d-%s", player.getID(), KEY_LIGHT));
				keys.add(String.format("%d-%s", player.getID(), KEY_RANGED));
				keys.add(String.format("%d-%s", player.getID(), KEY_HEAVY));
				keys.add(String.format("%d-%s", player.getID(), KEY_BASES));
				keys.add(String.format("%d-%s", player.getID(), KEY_BARRACKS));
				keys.add(String.format("%d-%s", player.getID(), KEY_RESOURCES));
			}
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		
		// first tests for common state features
		if(variableKey.equals(KEY_STAGE)) {
			return getStage();
		}
		
		// then breaks player;key to retrieve a player feature
		String key = (String) variableKey;
		String[] parts = key.split("-");
		
		try{
			return getPlayerFeature(Integer.parseInt(parts[0]), parts[1]);
		}
		catch(NullPointerException e){
			System.err.println("ERROR: player feature not found: " + variableKey);
		}
		
		return null;
	}

	@Override
	public State copy() {
		// if underlying game state has not changed, new object is identical
		return new AggregateState(underlyingState.clone());
	}
	
	/**
	 * Returns a String representation of state variable values
	 * To know the variable names, see {@link AggregateState#keysToString()}
	 */
	@Override
	public String toString(){
		String ret = keysToString() + "\n";
		for(Object key : variableKeys()){
			ret += get(key) + ";";
		}
		return ret;
	}
	
	/**
	 * Returns a String representation of state variable names
	 * @return
	 */
	public String keysToString(){
		String ret = "";
		for(Object key : variableKeys()){
			ret += key + ",";
		}
		return ret;
	}
	
	@Override
	public boolean equals(Object other){
		if(! (other instanceof AggregateState) ){
			return false;
		}
		
		// compares all state features
		AggregateState otherState = (AggregateState) other;
		
		for(Object feature : variableKeys()){
			
			if(! get(feature).equals(otherState.get(feature))){
				return false;
			}
		}
		
		return true;
	}

	public static List<AggregateState> enumerate(){
		
		
		return null;
	}
}
