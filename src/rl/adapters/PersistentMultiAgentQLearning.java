package rl.adapters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.JAQValue;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.statehashing.HashableState;
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
		Yaml yaml = new Yaml();
		BufferedWriter file = null;
		try {
			file = new BufferedWriter(new FileWriter(path));
			List<SGAgentType> agents = new ArrayList<>();
			for (MicroRTSState s : MicroRTSState.allStates()){
				agents.add(this.agentType);				
				for (JointAction ja : JointAction.getAllJointActionsFromTypes((State) s, agents)) {
					JAQValue value = this.myQSource.getQValueFor(s, ja);
					yaml.dump(value, file);
				}
			}	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void loadKnowledge(String path) {		
		Yaml yaml = new Yaml();
		try( InputStream in = Files.newInputStream(Paths.get(path))) {
			this.myQSource = (QSourceForSingleAgent) yaml.load(in);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
