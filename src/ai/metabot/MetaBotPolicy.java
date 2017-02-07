package ai.metabot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.SimpleHashableStateFactory;

public class MetaBotPolicy implements Policy {
	
	
	protected String pathToQ;
	/**
	 * Stores which action the policy recommends for each state
	 */
	Map<State, Action> greedyPolicy;
	
	public MetaBotPolicy(String pathToQ){
		QLearning qLearner;
		
		//discount = 0.9, defaultQ = 1, learningRate = 0.1
		qLearner = new QLearning(null, 0.9, new SimpleHashableStateFactory(false), 1, 0.1);
		
		//loads Q 'table' from given path
		this.pathToQ = pathToQ;
		qLearner.loadQTable(pathToQ);
		
		greedyPolicy = new HashMap<>();
		
		//for all states, stores the action with highest Q in greedyPolicy 
		for (State state : MicroRTSState.allStates()){
			List <QValue> qValues = qLearner.qValues(state);
			
			
			QValue maxQ = qValues.stream().max(Comparator.comparingDouble(qValue -> qValue.q)).get();//qValues.get(0);
			/*
			for(QValue currentQ : qValues){
				if(currentQ.q > maxQ.q){
					maxQ = currentQ;
				}
			}*/
			
			greedyPolicy.put(state, maxQ.a);
		}
		
	}

	@Override
	public Action action(State s) {
		return greedyPolicy.get(s);
	}

	@Override
	public double actionProb(State s, Action a) {
		if(a.equals(greedyPolicy.get(s))){
			return 1;
		}
		return 0;
	}

	@Override
	public boolean definedFor(State s) {
		return true;
	}


}
