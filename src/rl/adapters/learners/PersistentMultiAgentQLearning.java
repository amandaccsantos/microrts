package rl.adapters.learners;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import ai.metabot.learning.model.MicroRTSGame;
import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.JAQValue;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.statehashing.HashableStateFactory;
import rl.adapters.domain.EnumerableSGDomain;

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
		if(world == null){
			System.err.println(
				"ERROR: cannot save knowledge because world is null. Have I been initialized?"
			);
			return;
		}
		
		BufferedWriter fileWriter;
		try {
			fileWriter = new BufferedWriter(new FileWriter(path));

			if (domain instanceof EnumerableSGDomain){
				EnumerableSGDomain enumDomain = (EnumerableSGDomain) domain;
				for (State s : enumDomain.enumerate()) {
					fileWriter.write(String.format("state: %s\n", s));
					System.out.println(world);
					List<JointAction> jointActions = JointAction.getAllJointActions(
						s, 
						world.getRegisteredAgents()
					);
					
					for(JointAction jointAction : jointActions){
						fileWriter.write(String.format(
							"\t%s: %.3f\n", jointAction, getMyQSource().getQValueFor(s, jointAction).q
						));
					}
				}
			}
			else {
				System.err.println("Cannot save knowledge to this type of domain: " + domain.getClass().getName());
			}
		
		} catch (IOException e) {
			System.err.println("ERROR: Unable to save knowledge to file " + path);
			e.printStackTrace();
		}
		/*
		Yaml yaml = new Yaml();
		BufferedWriter file = null;
		try {
			file = new BufferedWriter(new FileWriter(path));
			List<SGAgentType> agents = new ArrayList<>();
			agents.add(this.agentType);	
			for (MicroRTSState s : MicroRTSState.allStates()){
				for (JointAction ja : JointAction.getAllJointActionsFromTypes((State) s, agents)) {
					JAQValue value = this.myQSource.getQValueFor(s, ja);
					yaml.dump(value, file);
				}
			}	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		*/
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
