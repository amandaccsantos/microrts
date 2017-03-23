package rl.adapters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.JAQValue;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.statehashing.HashableStateFactory;

/**
 * Extends {@link MultiAgentQLearning} class by implementing {@link PersistentLearner}
 * thus allowing saving and loading knowledge
 * @author anderson
 *
 */
public class PersistentMultiAgentQLearning extends MultiAgentQLearning implements PersistentLearner {

	public PersistentMultiAgentQLearning(SGDomain d, double discount, double learningRate,
			HashableStateFactory hashFactory, double qInit, SGBackupOperator backupOperator,
			boolean queryOtherAgentsForTheirQValues, String agentName, SGAgentType agentType) {
		super(d, discount, learningRate, hashFactory, qInit, backupOperator, queryOtherAgentsForTheirQValues, agentName,
				agentType);
	}

	public PersistentMultiAgentQLearning(SGDomain d, double discount, LearningRate learningRate,
			HashableStateFactory hashFactory, QFunction qInit, SGBackupOperator backupOperator,
			boolean queryOtherAgentsForTheirQValues, String agentName, SGAgentType agentType) {
		super(d, discount, learningRate, hashFactory, qInit, backupOperator, queryOtherAgentsForTheirQValues, agentName,
				agentType);
	}

	@Override
	public void saveKnowledge(String path) {
		try {
			PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
			List<SGAgentType> agents = new ArrayList<>();
			for (MicroRTSState s : MicroRTSState.allStates()){
				agents.add(this.agentType);
				
				for (JointAction ja : JointAction.getAllJointActionsFromTypes((State) s, agents)) {
					JAQValue value = this.myQSource.getQValueFor(s, ja);
					output.println(value);
				}
			}
			output.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void loadKnowledge(String path) {
		//FIXME implement this method
		System.err.println("NON-FATAL ERROR: method loadKnowledge of PersistentMultiAgentQLearning not implemented");
	}
}
