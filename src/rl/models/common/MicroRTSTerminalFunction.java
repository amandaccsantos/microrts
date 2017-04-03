package rl.models.common;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import rl.models.stages.GameStage;

public class MicroRTSTerminalFunction implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {
		GameStage state = (GameStage) s;
		return state.getUnderlyingState().gameover();
	}

}
