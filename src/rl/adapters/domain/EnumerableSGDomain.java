package rl.adapters.domain;

import java.util.List;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.SGDomain;

public abstract class EnumerableSGDomain extends SGDomain {

	public EnumerableSGDomain() {
		//do nothing
	}
	
	/**
	 * Returns a list with ALL states in this domain
	 * @return
	 */
	public abstract List<? extends State> enumerate();

}
