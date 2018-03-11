package rl.models.singleagentstages;

import java.util.Map;

import ai.core.AI;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointModel;
import rl.RLParamNames;
import rl.RLParameters;
import rl.exceptions.JAMExceptionLogger;
import rl.models.stages.GameStage;
import rl.models.stages.GameStages;
import rts.GameState;
import rts.PlayerAction;

public class SingleAgentStagesJAM implements JointModel {
	
	Map<String, AI> actions;
	AI opponent;
	int maxCycles;
	
	public SingleAgentStagesJAM(Map<String, AI> actions, AI opponent) {
		this.actions = actions;
		this.opponent = opponent;
		maxCycles = (int) RLParameters.getInstance().getParameter(RLParamNames.GAME_DURATION);
	}

	@Override
	public State sample(State s, JointAction ja) {
		//TODO = this seems to be common to all jointActionModels of abstract States 
		GameStage currentState = (GameStage)s;
		
		GameState gameState = currentState.getUnderlyingState().clone();
		
		boolean gameOver = false;
		boolean changedStage = false;	//stores whether game has moved to a different abstract state
		
		// the AIs selected by both players
		AI playerAIs[] = new AI[2];
		
		
		// instantiates the AI that player 1 selected (clones the object)
		playerAIs[0] = actions.get(ja.action(0).actionName()).clone();
		
		// second AI is fixed: our 'embedded' opponent
		playerAIs[1] = opponent.clone(); //actions.get(ja.action(1).actionName()).clone();

		// advance game until next state is reached or game finishes
		do {
			//for each player...
			for(int i = 0; i < playerAIs.length; i++){
				// ... gets its active AI
				AI ai = playerAIs[i];
				
				// ... then attempts to retrieve the actual microRTS action
				PlayerAction action = new PlayerAction();	//constructs one by default
				
				try {
					action = ai.getAction(i, gameState);
				} catch (Exception e) {
					System.err.println(
						String.format(
							"Frame: %d, Exception caught when getting action for player %d",
							gameState.getTime(), i
						)
					);
					
					// registers the exception
					JAMExceptionLogger.getInstance().registerException(
						e, i, "frame: " + gameState.getTime()
					);
				}
				
				// ... finally issues the action
				// if an exception occured, the 'empty' action is issued
				gameState.issueSafe(action);
			}
			
			// actions issued, simulate:
			gameOver = gameState.cycle();
			
			//checks whether any state variable has changed
			changedStage = ! currentState.equals(new GameStage(gameState)); 
			//System.out.print("\rFrame: " + gameState.getTime());
		} while (!gameOver && !changedStage && gameState.getTime() < maxCycles);
		
		//returns the new State associated with current underlying game state
		GameStage newState = new GameStage(gameState);
		
		//timeout is not checked inside GameStage constructor, set finished here
		if(gameState.getTime() >= maxCycles){
			newState.setStage(GameStages.FINISHED);
		}
		return newState; 
	}
	
}