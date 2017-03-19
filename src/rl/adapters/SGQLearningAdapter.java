package rl.adapters;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgentType;

/**
 * Adapts a single agent learning method to stochastic games
 * and provides saving and loading of knowledge.
 * So far, provides support for QLearning as the single agent learning method
 * @author anderson
 *
 */
public class SGQLearningAdapter extends LearningAgentToSGAgentInterface implements PersistentLearner {

	private QLearning learner;
	
	public SGQLearningAdapter(SGDomain domain, LearningAgent learningAgent, String agentName, SGAgentType agentType) {
		super(domain, learningAgent, agentName, agentType);
		
		if(learningAgent instanceof QLearning){
			learner = (QLearning) learningAgent;
		}
		else {
			throw new RuntimeException(
				"SGQLearningAdapter supports only QLearning as single agent learner"
			);
		}
	}

	@Override
	public void saveKnowledge(String path) {
		learner.writeQTable(path);
	}

	@Override
	public void loadKnowledge(String path) {
		learner.loadQTable(path);
	}

}
