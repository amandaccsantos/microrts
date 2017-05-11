package rl.models.singlestate;

import java.util.ArrayList;
import java.util.List;

import rl.models.stages.GameStage;
import rl.models.stages.GameStages;
import rts.GameState;

public class SingleState extends GameStage {
	
	GameStages stage;
	
	public SingleState() {
		super();
	}
	
	public SingleState(GameState gameState) {
		super(gameState);
		stage = GameStages.OPENING;		
	}

	public static GameStages frameToStage(int frameNumber){
		return GameStages.OPENING;
	}	
	
	@Override
	public static List<GameStage> allStates(){
		List<GameStage> states = new ArrayList<>();

		GameStage s = new GameStage();
		s.setStage(frameToStage(0));
		states.add(s);
		
		return states;
	}
	
}
