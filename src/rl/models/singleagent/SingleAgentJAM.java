package rl.models.singleagent;

import java.util.Map;

import rl.RLParamNames;
import rl.RLParameters;
import rl.models.aggregatediff.AggregateDiffState;
import rl.models.singlestate.SingleState;
import rl.models.stages.GameStages;
import rts.GameState;
import rts.PlayerAction;
import ai.core.AI;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.SampleModel;

public class SingleAgentJAM implements SampleModel {
	
	Map<String, AI> action;
	AI opponent;
	int maxCycles;
	
	public SingleAgentJAM(Map<String, AI> action, AI opponent) {
		this.action = action;
		this.opponent = opponent;
		maxCycles = (int) RLParameters.getInstance().getParameter(RLParamNames.GAME_DURATION);
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		AggregateDiffState state = (AggregateDiffState)s;
		
		GameState gameState = state.getUnderlyingState().clone();
		GameStages currentStage = state.getStage();// GameStages.OPENING;
		
		boolean gameOver = false;
		boolean changedStage = false;	//stores whether game has advanced a stage
		
		int delay = 0; //milisseconds
		long nextTimeToUpdate = System.currentTimeMillis() + delay;
		
		//instantiates the AIs that players selected (clones the objects)
		AI ai = action.get(a.actionName()).clone();
		
		//advance game until next stage is reached or game finishes
		do {
			if (System.currentTimeMillis() >= nextTimeToUpdate) {

				PlayerAction pa1 = null, pa2 = null;
				try {
					pa1 = ai.getAction(0, gameState);
					pa2 = opponent.getAction(1, gameState);
					
				} catch (Exception e) {
					System.err.println("An error happened when getting action for a player :(");
					e.printStackTrace();
				}
				
				gameState.issueSafe(pa1);
				gameState.issueSafe(pa2);

				// simulate:
				gameOver = gameState.cycle();
				
				//checks whether game has advanced to a new stage
				changedStage = currentStage != SingleAgent.frameToStage(gameState.getTime()); 
				
				//w.repaint();
				nextTimeToUpdate += delay;
			}
		} while (!gameOver && !changedStage && gameState.getTime() < maxCycles);
		
		//returns the new State, with a 'finished' on it
		SingleState currentState = new SingleState(gameState); 
		currentState.setStage(GameStages.FINISHED);
		
		EnvironmentOutcome theNewState = new EnvironmentOutcome(state, a, currentState, gameState.winner(), true); 
		//System.out.println(theNewState);
		return theNewState; 
	}

	@Override
	public boolean terminal(State s) {
		AggregateDiffState state = (AggregateDiffState)s;
		
		GameStages currentStage = state.getStage();
		if (currentStage == GameStages.FINISHED)
			return true;
		else
			return false;
	}
	
}
