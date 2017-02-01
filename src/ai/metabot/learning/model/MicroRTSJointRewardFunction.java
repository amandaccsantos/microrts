package ai.metabot.learning.model;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import rts.GameState;

public class MicroRTSJointRewardFunction implements JointRewardFunction {

	@Override
	public double[] reward(State s, JointAction ja, State sp) {
		MicroRTSState resultingState = (MicroRTSState) sp;
		GameState underlyingState = resultingState.getUnderlyingState();
		if (! underlyingState.gameover()) {
			return new double[]{0, 0};
		}
		
		else if (underlyingState.winner() == -1) { //DRAW
			return new double[]{0, 0};
		}
		else if (underlyingState.winner() == 0) { //victory of first player
			return new double[]{1, -1};
		}
		else { //victory of 2nd player
			return new double[]{-1, 1};
		}
	}

}
