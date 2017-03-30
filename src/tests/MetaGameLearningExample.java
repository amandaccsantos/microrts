package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import ai.abstraction.LightRush;
import ai.metabot.DummyPolicy;
import ai.metabot.learning.model.MicroRTSGame;
import ai.metabot.learning.model.MicroRTSJointRewardFunction;
import ai.metabot.learning.model.MicroRTSState;
import ai.metabot.learning.model.MicroRTSTerminalFunction;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.auxiliary.GameSequenceVisualizer;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import rl.RLParamNames;
import rl.RLParameters;
import rl.adapters.learners.SGQLearningAdapter;
import tests.rl.RLParametersTest;

/**
 * An example of the Algorithm Selection Metagame in microRTS
 * 
 * @author anderson
 *
 */
public class MetaGameLearningExample {
	public MetaGameLearningExample() {
		MicroRTSGame microRTSGame = null;
		try {
			microRTSGame = new MicroRTSGame();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SGDomain microRTSDomain = (SGDomain) microRTSGame.generateDomain();
		SGAgentType agentType = new SGAgentType("agent", microRTSDomain.getActionTypes());

		JointRewardFunction rwdFunc = new MicroRTSJointRewardFunction();
		TerminalFunction terminalFunc = new MicroRTSTerminalFunction();

		World w = new World(microRTSDomain, rwdFunc, terminalFunc, microRTSGame.getInitialState());
		
		RLParameters rlParams = RLParameters.getInstance();
		
		Map<String, Object> parameters = null;
		try {
			parameters = rlParams.loadFromFile("experiments/example_SGQLearningAdapter_Light.xml");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //may throw exceptions
				
		int ngames = (int) parameters.get(RLParamNames.EPISODES);
		List<GameEpisode> episodes = new ArrayList<GameEpisode>(ngames);
	
		List<SGAgent> players = (List<SGAgent>) parameters.get(RLParamNames.PLAYERS);
		
		QLearning ql1 = null;
		QLearning ql2 = null;
		
		SGQLearningAdapter a1 = null;
		SGQLearningAdapter a2 = null;
		
		for(SGAgent player : players){			
			
			SGQLearningAdapter sgql = (SGQLearningAdapter) player;
			if (sgql.agentName().equals("learner")){ // instanceof PersistentMultiAgentQLearning){
				final float discount = ((float) parameters.get(RLParamNames.DISCOUNT));
				final float learningRate = ((float) parameters.get(RLParamNames.LEARNING_RATE));
				final float defaultQ = ((float) parameters.get(RLParamNames.INITIAL_Q));
				
				ql1 = new QLearning(null, discount, new SimpleHashableStateFactory(false), defaultQ, learningRate);
				
				// create a single-agent interface for each of our learning algorithm instances
				a1 = new SGQLearningAdapter(microRTSDomain, ql1, "agent0", new SGAgentType("SGQLearningAdapter", w.getDomain().getActionTypes()));
			}
			else if (player.agentName().equals("dummy")){ // instanceof SGQLearningAdapter){	//both agents in example are SGQLearningAdapter
				//casts the player and tests its attributes 
				ql2 = new QLearning(null, 0, new SimpleHashableStateFactory(false), 0, 0);
						
				String policy = (String) parameters.get(RLParamNames.DUMMY_POLICY);
				// ql2 will be a dummy, always selecting the same behavior
				ql2.setLearningPolicy(new DummyPolicy(policy, ql2));
				
				Field policyField = null;
				try {
					policyField = ql2.getClass().getDeclaredField("learningPolicy");
				} catch (NoSuchFieldException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				policyField.setAccessible(true);
				Policy thePolicy = null;
				try {
					thePolicy = (Policy) policyField.get(ql2);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// create a single-agent interface for each of our learning algorithm instances
				a2 = new SGQLearningAdapter(microRTSDomain, ql2, "agent1", new SGAgentType("Dummy", w.getDomain().getActionTypes()));
			}
		}
		
		w.join(a1);
		w.join(a2);

		// don't have the world print out debug info (comment out if you want to
		// see it!)
		DPrint.toggleCode(w.getDebugId(), false);

		System.out.println("Starting training");
		
		PrintWriter output = null;
		
		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter("output.txt", false)));
			for (int i = 0; i < ngames; i++) {
				GameEpisode episode = w.runGame();
				
				episodes.add(episode);
				if (i % 10 == 0) {
					System.out.println("Game: " + i + ": " + episode.maxTimeStep());
				}
				episode.write("/tmp/qltest/qltest_" + i);
				ql1.writeQTable("/tmp/qltest/qtable0_" + i);
				ql2.writeQTable("/tmp/qltest/qtable1_" + i);
				
				output.println("Game: " + i);
				output.println("Value functions for agent 0");

				for (MicroRTSState s : MicroRTSState.allStates()) {
					output.println(String.format("%s: %.3f", s, ql1.value(s)));
					for (QValue q : ql1.qValues(s)) {
						output.println(String.format("%s: %.3f", q.a, q.q));
					}
				}

				output.println("Value functions for agent 1");
				for (MicroRTSState s : MicroRTSState.allStates()) {
					output.println(String.format("%s: %.3f", s, ql2.value(s)));
					for (QValue q : ql2.qValues(s)) {
						output.println(String.format("%s: %.3f", q.a, q.q));
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		output.close();
	}

	public static void main(String[] args) {

		new MetaGameLearningExample();

		try {
			Runtime.getRuntime().exec("python python/plot_actions.py");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
