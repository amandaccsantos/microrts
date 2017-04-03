package rl.models.stages;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.state.State;
import rl.models.MicroRTSState;
import rts.GameState;

public class GameStage extends MicroRTSState{
	
	GameStages stage;
	
	
	/**
	 * Duration of a stage, in microRTS frames
	 */
	public static final int STAGE_DURATION = 600;
	
	/**
	 * 'name' of the stage field
	 */
	public static final String KEY_STAGE = "stage";
	
	/**
	 * The underlying game state related to this state
	 */
	protected GameState gameState;
	
	/**
	 * An attempt to make the object serializable 
	 * (must have default constructor and get/set methods 
	 */
	public GameStage(){
		super();
	}
	
	/**
	 * Constructs a state based on a microRTS game state
	 * @param gameState the microRTS game state 
	 */
	public GameStage(GameState gameState) {
		this.gameState = gameState;
		
		//if game is over, stage is FINISHED
		if(gameState.gameover()) {
			stage = GameStages.FINISHED;
		}
		
		//otherwise get the stage according to the time
		else {
			stage = frameToStage(gameState.getTime());
		}
	}
	
	/**
	 * Returns the GameStage that this state refers to
	 * @return
	 */
	public GameStages getStage(){
		return stage;
	}
	
	
	public void setStage(GameStages theStage){
		stage = theStage;
	}
	
	/**
	 * Alias of {@link getUnderlyingGameState} for the sake of serializability
	 * @return
	 *
	public GameState getGameState(){
		return getUnderlyingState();
	}
	
	public void setGameState(GameState theState){
		gameState = theState;
	}*/
	
	/**
	 * Returns all possible states
	 * @return
	 */
	public static List<GameStage> allStates(){
		List<GameStage> states = new ArrayList<>();
		
		for(int frameNumber = 0; frameNumber < 3000; frameNumber += STAGE_DURATION){
			GameStage s = new GameStage();
			s.stage = frameToStage(frameNumber);
			states.add(s);
		}
		
		return states;
	}
	
	/**
	 * Returns the GameStage that this frame is in
	 * @param frameNumber
	 * @return
	 */
	public static GameStages frameToStage(int frameNumber){
		if(frameNumber < STAGE_DURATION){
			return GameStages.OPENING;
		}
		else if (frameNumber < 2 * STAGE_DURATION){
			return GameStages.EARLY;
		}
		else if (frameNumber < 3 * STAGE_DURATION){
			return GameStages.MID;
		}
		else if (frameNumber < 4 * STAGE_DURATION){
			return GameStages.LATE;
		}
		else {
			return GameStages.END;
		}
	}
	/**
	 * Returns the microRTS game state related to high-level state
	 * @return
	 */
	public GameState getUnderlyingState(){
		return gameState;
	}
	
	public boolean equals(Object other){
		if(! (other instanceof GameStage) ){
			return false;
		}
		return stage == ((GameStage)other).getStage();
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>();
		
		keys.add(KEY_STAGE);
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if (variableKey.equals(KEY_STAGE)){
			return stage;
		}
		return null;
	}

	@Override
	public State copy() {
		return new GameStage(gameState.clone());
	}
	
	public String toString(){
		return stage.toString();
	}
	
	
}
