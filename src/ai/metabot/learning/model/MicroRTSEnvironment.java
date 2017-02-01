package ai.metabot.learning.model;

import ai.core.AI;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.world.World;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

public class MicroRTSEnvironment implements Environment {

	AI player1;
	AI player2;
	
	@Override
	public State currentObservation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvironmentOutcome executeAction(Action arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInTerminalState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double lastReward() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetEnvironment() {
		// TODO Auto-generated method stub

	}

}
