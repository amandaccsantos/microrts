package ai.metabot.learning.model;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.state.State;
import rts.GameState;

enum GameStage{
	OPENING, 	//0 - 599 frames
	EARLY,		//600 - 1199
	MID,		//1200 - 1799
	LATE,		//1800 - 2399
	END			//2400 - 3000
}

public class MicroRTSState implements State{
	
	GameStage stage;
	
	public static int STAGE_DURATION = 600;
	
	/**
	 * The underlying game state related to this state
	 */
	GameState gameState;
	
	/**
	 * Constructs a state based on a microRTS game state
	 * @param gameState the microRTS game state 
	 */
	public MicroRTSState(GameState gameState) {
		this.gameState = gameState;
		
		//this.frameNumber = gameState.getTime();
		
		stage = frameToStage(gameState.getTime());
	}
	
	public GameStage getStage(){
		return stage;
	}
	
	public static GameStage frameToStage(int frameNumber){
		if(frameNumber < STAGE_DURATION){
			return GameStage.OPENING;
		}
		else if (frameNumber < 2 * STAGE_DURATION){
			return GameStage.EARLY;
		}
		else if (frameNumber < 3 * STAGE_DURATION){
			return GameStage.MID;
		}
		else if (frameNumber < 4 * STAGE_DURATION){
			return GameStage.LATE;
		}
		else {
			return GameStage.END;
		}
	}
	
	/*public static GameStage nextStage(G){
		
	}*/
	
	/**
	 * Returns the microRTS game state related to high-level state
	 * @return
	 */
	public GameState getUnderlyingState(){
		return gameState;
	}
	
	public boolean equals(Object other){
		if(! (other instanceof MicroRTSState) ){
			return false;
		}
		return stage == ((MicroRTSState)other).getStage();
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>();
		
		keys.add(GameStage.class);
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if (variableKey.equals(GameStage.class)){
			return stage;
		}
		return null;
	}

	@Override
	public State copy() {
		return new MicroRTSState(gameState.clone());
	}
	
	
}
