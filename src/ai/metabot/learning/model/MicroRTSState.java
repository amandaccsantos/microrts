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
	int frameNumber;
	
	GameStage stage;
	
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
		
		this.frameNumber = gameState.getTime();
		
		if(frameNumber < 600){
			stage = GameStage.OPENING;
		}
		else if (frameNumber < 1200){
			stage = GameStage.EARLY;
		}
		else if (frameNumber < 1800){
			stage = GameStage.MID;
		}
		else if (frameNumber < 2400){
			stage = GameStage.LATE;
		}
		else {
			stage = GameStage.END;
		}
	}
	
	public GameStage getStage(){
		return stage;
	}
	
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
