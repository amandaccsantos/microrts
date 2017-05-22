package ai.metagame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

public class RandomPolicy implements Policy {

	List<String> actions;
	String randomAction;
	protected QLearning qLearner;
	protected Random rand = RandomFactory.getMapped(0);

	public RandomPolicy(QLearning learner) {
		this.actions = new ArrayList<String>();
		this.qLearner = learner;

		actions.add("HeavyRush");
		actions.add("LightRush");
		actions.add("RangedRush");
		actions.add("WorkerRush");
		
		this.randomAction = actions.get(this.rand.nextInt(this.actions.size()));
	}

	@Override
	public Action action(State s) {
		// traverses the list of qValues to find which action corresponds to pre-defined behavior
		List<QValue> qValues = qLearner.qValues(s);

		for (QValue qValue : qValues) {
			if (qValue.a.actionName().equals(this.randomAction)) {
				return qValue.a;
			}
		}
		// should not get here!
		System.err.println(String.format("Behavior %s not found in state %s!", this.randomAction, s));
		return null;
	}

	@Override
	public double actionProb(State s, Action a) {
		if(a.actionName().equals(this.randomAction)){
			return 1;
		}
		return 0;
	}

	@Override
	public boolean definedFor(State s) {
		return true;
	}

}
