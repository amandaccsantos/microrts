package rl.models.stages;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

public class MicroRTSTerminalFunction implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {
		MicroRTSState state = (MicroRTSState) s;
		return state.getUnderlyingState().gameover();
	}

}
