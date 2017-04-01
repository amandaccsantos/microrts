package rl.models;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import rl.models.stages.GameStage;
import rts.GameState;

public class WinLossRewardFunction implements JointRewardFunction {

	@Override
	/**
	 * Reward is zero for draws and non-terminals; otherwise +1 for victory, -1 for defeat
	 */
	public double[] reward(State s, JointAction ja, State sp) {
		GameStage resultingState = (GameStage) sp;
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