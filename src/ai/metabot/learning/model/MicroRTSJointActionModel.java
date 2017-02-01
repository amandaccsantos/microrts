package ai.metabot.learning.model;

import java.util.Map;

import ai.RandomAI;
import ai.core.AI;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointModel;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class MicroRTSJointActionModel implements JointModel {
	
	Map<String, AI> actions;
	
	public MicroRTSJointActionModel(Map<String, AI> actions) {
		this.actions = actions;
	}
	

	@Override
	public State sample(State s, JointAction ja) {
		MicroRTSState state = (MicroRTSState)s;
		//I won't check if state is terminal here
		
		GameState gameState = state.getUnderlyingState().clone();
		GameStage currentStage = state.getStage();
		
		boolean gameOver = false;
		boolean changedStage = false;	//stores whether game has advanced a stage
		
		long nextTimeToUpdate = System.currentTimeMillis() + MicroRTSGame.PERIOD;
		do {
			if (System.currentTimeMillis() >= nextTimeToUpdate) {
				AI ai1 = actions.get(ja.action(0).actionName());
				AI ai2 = actions.get(ja.action(1).actionName());
				
				System.out.println("Actions: P1: " + ai1 + " / P2: " + ai2);

				PlayerAction pa1 = null, pa2 = null;
				try {
					pa1 = ai1.getAction(0, gameState);
					pa2 = ai2.getAction(1, gameState);
					
				} catch (Exception e) {
					System.err.println("An error happened when getting action for a player :(");
					e.printStackTrace();
				}
				
				
				gameState.issueSafe(pa1);
				gameState.issueSafe(pa2);

				// simulate:
				gameOver = gameState.cycle();
				
				//checks whether game has advanced to a new stage
				changedStage = currentStage != MicroRTSState.frameToStage(gameState.getTime()); 
				
				//w.repaint();
				nextTimeToUpdate += MicroRTSGame.PERIOD;
			} else {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} while (!gameOver && !changedStage && gameState.getTime() < MicroRTSGame.MAXCYCLES);
		
		//returns the new State associated with current underlying game state
		return new MicroRTSState(gameState); 
		
	}

}
