package rl.models;

import java.util.List;

import ai.metabot.learning.model.MicroRTSState;
import burlap.mdp.core.state.State;
import rl.adapters.domain.EnumerableSGDomain;

public class GameStagesDomain extends EnumerableSGDomain {

	public GameStagesDomain() {
		//do nothing
	}

	@Override
	public List<? extends State> enumerate() {
		return MicroRTSState.allStates();
	}

}
