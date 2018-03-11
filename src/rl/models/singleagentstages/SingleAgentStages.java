package rl.models.singleagentstages;

import rl.models.stages.GameStage;
import rl.models.stages.GameStages;
import rts.GameState;
import burlap.mdp.core.state.State;

/**
 * The embedded-adversary model, with state abstraction 
 * based on game stages
 *
 */
public class SingleAgentStages extends GameStage {

	public SingleAgentStages() {
		super();
	}

	public SingleAgentStages(GameState gameState) {
		super(gameState);
	}
	
	@Override
	public State copy() {
		SingleAgentStages theCopy = new SingleAgentStages(getUnderlyingState().clone());
		if (this.stage == GameStages.FINISHED) {
			theCopy.setStage(GameStages.FINISHED);
		}
		return theCopy;
	}
}
